package com.zzccaidp.service.knowledgebase;

import cn.hutool.core.bean.BeanUtil;
import com.zzccaidp.common.AmazonS3Util;
import com.zzccaidp.dao.knowledgebase.DocTypeDO;
import com.zzccaidp.dao.knowledgebase.DocumentDO;
import com.zzccaidp.enums.ChunkMethodEnum;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.exception.BusinessException;
import com.zzccaidp.integration.ragflow.RagFlowIntegrationService;
import com.zzccaidp.integration.ragflow.request.CreateDataSetRequest;
import com.zzccaidp.integration.ragflow.response.CreateDataSetResponse;
import com.zzccaidp.mapper.knowledgebase.DocTypeMapper;
import com.zzccaidp.service.ragFlow.RagFlowService;
import com.zzccaidp.mapper.knowledgebase.DocumentMapper;
import com.zzccaidp.vo.knowledgebase.DocTypeIn;
import com.zzccaidp.vo.knowledgebase.DocTypeOut;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DocTypeService {

    @Autowired
    private DocTypeMapper docTypeMapper;

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private RagFlowService ragFlowService;

    @Autowired
    private RagFlowIntegrationService ragFlowIntegrationService;

    @Autowired
    private AmazonS3Util amazonS3Util;

    @Value("${bades.file.aws-s3.bucketName}")
    private String backName;

    /**
     * 资源库 dataset 使用的嵌入模型（必须与公共库/个人库一致，否则向量空间不匹配导致跨库检索错乱）
     */
    @Value("${ragFlow.embeddingModel:BAAI/bge-large-zh-v1.5}")
    private String embeddingModel;

    public List<DocTypeDO> findAll(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return docTypeMapper.selectAll();
        }
        return docTypeMapper.searchByKeyword(keyword);
    }

    public String selectDocChannel(String docTypeCode) {
        List<String> list = documentMapper.selectDocChannel(docTypeCode);
        return list.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",", "[", "]"));
    }

    public DocTypeOut findById(String id) {
        docTypeMapper.selectById(id);
        return BeanUtil.copyProperties(docTypeMapper.selectById(id), DocTypeOut.class);
    }

    public DocTypeDO findByCode(String code) {
        return docTypeMapper.selectByCode(code);
    }

    /**
     * 创建资源库（文档类型）：先联动 ragflow 创建远程 dataset，成功后将 dataset_id 一并落库。
     * <p>
     * 参照个人知识库 {@link PersonalDatasetService#getOrCreatePersonalDataset} 的创建模式：
     * ragflow 创建失败（网络异常/响应异常/data.id 为空）直接抛 BusinessException，
     * 事务回滚后本地不落库，避免产生无 dataset 的资源库导致后续文档向量化报「数据集不存在」。
     */
    @Transactional(rollbackFor = Exception.class)
    public DocTypeDO create(DocTypeIn request) {
        // 切片方式校验：空值默认 naive（通用）；非法值本地拦截，避免透传 ragflow 被 pydantic 校验拒绝
        String chunkMethod = resolveChunkMethod(request.getChunkMethod());

        // 联动 ragflow 创建远程 dataset：名称用「code_名称」保证唯一（ragflow 拒绝重名 dataset）
        CreateDataSetRequest dsRequest = new CreateDataSetRequest();
        dsRequest.setName(request.getCode() + "_" + request.getName());
        dsRequest.setDescription(StringUtils.isNotBlank(request.getDescription()) ? request.getDescription() : request.getName());
        dsRequest.setEmbeddingModel(embeddingModel);
        // 公共资源库语义：团队可见（检索走统一管理 API Key，permission 不影响接口访问）
        dsRequest.setPermission("team");
        dsRequest.setChunkMethod(chunkMethod);

        CreateDataSetResponse dsResponse = ragFlowIntegrationService.createDataSet(dsRequest);
        if (dsResponse == null || StringUtils.isBlank(dsResponse.getId())) {
            log.error("创建资源库数据集失败，code：{}，name：{}", request.getCode(), request.getName());
            throw new BusinessException(ErrCodeEnum.M7007);
        }

        DocTypeDO docType = new DocTypeDO();
        docType.setId(UUID.randomUUID().toString());
        docType.setName(request.getName());
        docType.setCode(request.getCode());
        docType.setDescription(request.getDescription());
        docType.setCount(0);
        // 落库切片方式与 ragflow dataset 保持一致（doc_type 旧业务字典已废弃，统一 ragflow 字典）
        docType.setChunkMethod(chunkMethod);
        docType.setDatasetId(dsResponse.getId());
        docType.setSourceCount(0);
        docType.setUpdateTime(new Date());
        docType.setStatus("active");
        docTypeMapper.insert(docType);
        return docType;
    }

    /**
     * 更新资源库：切片方式创建后不可修改——ragflow 修改 chunk_method 仅影响后续新文档解析，
     * 存量已解析文档不会重新分片，允许变更易造成「dataset 配置已改、存量切片仍旧策略」的不一致，
     * 故请求值与库中不一致时直接拒绝；值相同（含历史值回传）忽略不处理。
     */
    @Transactional(rollbackFor = Exception.class)
    public DocTypeDO update(String code, DocTypeIn request) {
        DocTypeDO docType = docTypeMapper.selectByCode(code);
        if (docType == null) {
            return null;
        }
        if (request.getName() != null) {
            docType.setName(request.getName());
        }
        if (request.getDescription() != null) {
            docType.setDescription(request.getDescription());
        }
        if (request.getCode() != null) {
            docType.setCode(request.getCode());
        }
        // 切片方式锁定：与库中值不一致直接拒绝，防止绕过前端只读限制从接口侧修改
        if (request.getChunkMethod() != null && !request.getChunkMethod().equals(docType.getChunkMethod())) {
            throw new BusinessException(ErrCodeEnum.M0005, "切片方式创建后不可修改");
        }
        if (request.getDatasetId() != null) {
            docType.setDatasetId(request.getDatasetId());
        }
        // 检索开关：null 不更新（兼容旧调用方只改其他字段的场景），0/1 透传落库
        if (request.getRetrievalEnabled() != null) {
            docType.setRetrievalEnabled(request.getRetrievalEnabled());
        }
        docType.setUpdateTime(new Date());
        docTypeMapper.updateByPrimaryKey(docType);
        return docType;
    }

    /**
     * 切片方式合法性校验与默认值处理。
     *
     * @param chunkMethod 请求传入的切片方式
     * @return 合法取值；空值返回默认 naive（通用）
     * @throws BusinessException 非法取值时本地拦截（错误消息透传前端）
     */
    private String resolveChunkMethod(String chunkMethod) {
        if (StringUtils.isBlank(chunkMethod)) {
            return ChunkMethodEnum.NAIVE.getValue();
        }
        if (!ChunkMethodEnum.isValid(chunkMethod)) {
            throw new BusinessException(ErrCodeEnum.M0005, "不支持的切片方式：" + chunkMethod);
        }
        return chunkMethod;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean delete(String id) {
        DocTypeDO docType = docTypeMapper.selectById(id);
        if (docType == null) {
            return false;
        }
        try {
            String datasetId = docType.getDatasetId();
            if(datasetId != null && !datasetId.isEmpty()) {
                boolean success = ragFlowService.deleteRagDataset(docType);
                if (!success) {
                    log.error("删除ragFlow数据集失败");
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return false;
                }
                docType.setDatasetId(null);
                docTypeMapper.updateByPrimaryKey(docType);

                // RagFlow 删除 dataset 仅清向量与自身 DB 记录，不删其存储中的物理文件
                // （ragflow-0.27.1 internal/service/dataset/crud.go#deleteDataset 无 storage 删除调用）。
                // 本部署为独立桶形态：每个 dataset 独占「桶名=dataset_id」的桶，
                // 整桶清删彻底回收（含解析图片/chunk图片）；失败仅告警不回滚（残留仅占存储）
                try {
                    amazonS3Util.purgeRagFlowDatasetFiles(datasetId);
                } catch (Exception purgeEx) {
                    log.warn("清理 ragflow 存储独立桶失败（残留仅占存储，可人工处理），datasetId={}", datasetId, purgeEx);
                }
            }

            //删除关联文档
            DocumentDO queryDO = new DocumentDO();
            queryDO.setDocType(docType.getCode());
            List<DocumentDO> documentDOList = documentMapper.selectByCondition(queryDO);
            List<String> fastDfsFileIdList = documentDOList.stream().
                    map(DocumentDO::getBucketPath)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            if (!fastDfsFileIdList.isEmpty()) {
                amazonS3Util.batchDeleteFile(fastDfsFileIdList, backName);
            }
            //变更历史记录保留（doc_change_record 作为推送审计痕迹，不随类型删除清理）
            List<String> docIds = documentDOList.stream()
                    .map(DocumentDO::getDocId)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            documentMapper.batchDeleteByDocTypeId(docType.getCode());
            //删除数据集
            docTypeMapper.deleteById(id);

            log.info("删除文档类型及关联数据完成, docType={}, 文档数={}, 变更记录保留docId数={}, ragflow存储已清理={}",
                    docType.getCode(), documentDOList.size(), docIds.size(), datasetId != null && !datasetId.isEmpty());
            return true;
        } catch (Exception e) {
            log.error("删除文档失败",e );
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }

    public List<DocTypeDO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return docTypeMapper.selectAll();
        }
        return docTypeMapper.searchByKeyword(keyword);
    }

    @Transactional
    public DocTypeDO updateChunkMethod(String id, String chunkMethod) {
        DocTypeDO docType = docTypeMapper.selectById(id);
        if (docType != null) {
            docTypeMapper.updateChunkMethod(id, chunkMethod);
            docType.setChunkMethod(chunkMethod);
            docType.setUpdateTime(new Date());
        }
        return docType;
    }

    public Integer selectDocCountByDocType(String code) {
        return documentMapper.selectDocCountByDocType(code);
    }

    public Integer selectDataSourceCountByDocType(String code) {
        List<String> list = documentMapper.selectDataSourceCountByDocType(code);
        return list.size();
    }
}