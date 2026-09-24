package com.zzccaidp.service.ragFlow;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.zzccaidp.constants.RagFlowConstant;
import com.zzccaidp.dao.knowledgebase.DocTypeDO;
import com.zzccaidp.dao.knowledgebase.DocumentDO;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.exception.BusinessException;
import com.zzccaidp.facade.AidpDocumentServiceImpl;
import com.zzccaidp.in.DocumentAddIn;
import com.zzccaidp.integration.ragflow.RagFlowIntegrationService;
import com.zzccaidp.integration.ragflow.dto.MetadataDelete;
import com.zzccaidp.integration.ragflow.dto.MetadataUpdate;
import com.zzccaidp.integration.ragflow.dto.UpdateMetadataSelector;
import com.zzccaidp.integration.ragflow.request.ChunkDocumentRequest;
import com.zzccaidp.integration.ragflow.request.UpdateDocMetadataRequest;
import com.zzccaidp.integration.ragflow.request.UpdateDocumentRequest;
import com.zzccaidp.integration.ragflow.request.UploadDocumentRequest;
import com.zzccaidp.integration.ragflow.response.DeleteDatasetResponse;
import com.zzccaidp.integration.ragflow.response.DeleteDocumentResponse;
import com.zzccaidp.integration.ragflow.response.RagFlowResponse;
import com.zzccaidp.integration.ragflow.response.UploadDocumentResponse;
import com.zzccaidp.mapper.knowledgebase.DocTypeMapper;
import com.zzccaidp.mapper.knowledgebase.DocumentMapper;
import com.zzccaidp.service.ragFlow.dto.RagFlowPipelineDTO;
import com.zzccaidp.util.CollectUtil;
import com.zzccaidp.util.StringUtil;
import com.zzccaidp.vo.knowledgebase.RagFlowDocumentDetail;
import com.zzccaidp.vo.knowledgebase.RagFlowDocumentListDetail;
import io.lettuce.core.protocol.CompleteableCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author zhangtiantian
 * @date 2026/5/8
 */
@Service
@Slf4j
public class RagFlowServiceImpl implements RagFlowService {

    /** ragflow 文档主名（不含扩展名）最大字符数，超出则截断并追加省略号 */
    private static final int RAGFLOW_FILE_NAME_MAX_LENGTH = 200;

    /** 个人知识库文档类型标识（与 PersonalDocumentService.PERSONAL_DOC_TYPE 保持一致） */
    private static final String PERSONAL_DOC_TYPE = "personal";

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private DocTypeMapper docTypeMapper;

    @Autowired
    private RagFlowIntegrationService ragFlowIntegrationService;

    @Autowired
    private AidpDocumentServiceImpl aidpDocumentService;


    @Override
    public void file2RagFlowPipeline(RagFlowPipelineDTO ragFlowPipelineDTO) {

        // 查询文档信息
        DocumentDO docInfo = this.getDocInfo(ragFlowPipelineDTO.getDocId());

        // 删除已有文件
        this.deleteRagDocument(docInfo.getVectorId(), docInfo.getDatasetId());

        // 上传文件
        UploadDocumentResponse uploadDocumentResponse = this.uploadDocument2RagFlow(docInfo);

        // 设置向量Id
        docInfo.setVectorId(uploadDocumentResponse.getId());

        // 设置元数据
        this.updateDocument2RagFlow(docInfo);

        // chunks
        this.chunkDocument2RagFlow(docInfo, ragFlowPipelineDTO);

        // 保存
        this.saveDocument(docInfo);
    }

    /**
     * 查询文档信息，并按渠道分流获取 datasetId（datasetId 是 ragflow 的一人一库/公共库标识）。
     * <p>
     * 公共库文档：走 {@code selectByDocId}（JOIN doc_type），datasetId 取自 doc_type.dataset_id；
     * 个人知识库文档（一人一库，docType='personal'）：doc_type 表中没有与之关联的记录，
     * 公共 SQL 的 INNER JOIN 会查不到文档，因此改走 {@code selectPersonalByDocId}
     * （不关联 doc_type），datasetId 取文档自身的 own_dataset_id。
     * <p>
     * datasetId 仍为空说明文档没有可用的向量库归属：清空 vectorId 落库后抛 M7005。
     *
     * @param docId 业务文档ID
     * @return 文档信息，datasetId 已按渠道填充
     */
    private DocumentDO getDocInfo(String docId) {
        // 公共库：datasetId 依赖 doc_type.dataset_id
        DocumentDO sourceDoc = documentMapper.selectByDocId(docId);
        // 命中个人库文档，或公共 SQL 因 doc_type 无 personal 关联记录而查不到时，
        // 都改走个人库专用 SQL（已过滤 doc_type='personal'）：不 JOIN doc_type，
        // datasetId 取文档自身的 own_dataset_id
        if (sourceDoc == null || PERSONAL_DOC_TYPE.equals(sourceDoc.getDocType())) {
            DocumentDO personalDoc = documentMapper.selectPersonalByDocId(docId);
            if (personalDoc != null) {
                sourceDoc = personalDoc;
                sourceDoc.setDatasetId(personalDoc.getOwnDatasetId());
            }
        }
        if (sourceDoc == null) {
            log.error("文档不存在 :{}", docId);
            throw new BusinessException(ErrCodeEnum.M7102);
        }
        if (StringUtil.isBlank(sourceDoc.getDatasetId())) {
            log.error("文档数据集不存在 :{}", docId);
            sourceDoc.setVectorId(null);
            documentMapper.updateByPrimaryKey(sourceDoc);
            throw new BusinessException(ErrCodeEnum.M7005);
        }
        return sourceDoc;
    }

    @Override
    public boolean deleteRagDocument(String docId) {
        try {
            DocumentDO sourceDoc = this.getDocInfo(docId);
            return this.deleteRagDocument(sourceDoc.getVectorId(), sourceDoc.getDatasetId());
        } catch (Exception e) {
            log.error("删除ragFlow文件失败", e);
            return false;
        }
    }
    @Override
    public boolean deleteRagDataset(DocTypeDO docTypeDO) {
        try {
            return this.deleteRagDataset(docTypeDO.getDatasetId());
        } catch (Exception e) {
            log.error("删除ragFlow文件失败", e);
            return false;
        }
    }

    @Override
    public void updateMetadata(String docId, Map<String, Object> oldMetadata) {
        DocumentDO sourceDoc = this.getDocInfo(docId);
        this.updateDocumentMetaData2RagFlow(sourceDoc, oldMetadata);
    }

    private void updateDocumentMetaData2RagFlow(DocumentDO sourceDoc, Map<String, Object> oldMetadata) {
        if (StringUtil.isBlank(sourceDoc.getVectorId()) || StringUtil.isBlank(sourceDoc.getDatasetId())) {
            log.info("文档-{}-未向量化无需更新", sourceDoc.getName());
            return;
        }

        UpdateDocMetadataRequest updateDocMetadataRequest = new UpdateDocMetadataRequest();
        updateDocMetadataRequest.setDatasetId(sourceDoc.getDatasetId());
        UpdateMetadataSelector updateMetadataSelector = new UpdateMetadataSelector();
        updateMetadataSelector.setDocumentIds(Collections.singletonList(sourceDoc.getVectorId()));
        updateDocMetadataRequest.setSelector(updateMetadataSelector);
        // 先清空已有的
        if (!CollectUtil.isEmpty4Map(oldMetadata)) {
            log.info("清空文档-{}-已有元数据：{}", sourceDoc.getName(), sourceDoc.getMetadata());
            Set<String> keys = oldMetadata.keySet();
            List<MetadataDelete> deletes = keys.stream().map(key -> {
                MetadataDelete metadataDelete = new MetadataDelete();
                metadataDelete.setKey(key);
                return metadataDelete;
            }).collect(Collectors.toList());
            updateDocMetadataRequest.setDeletes(deletes);
            ragFlowIntegrationService.updateDocumentMetadata(updateDocMetadataRequest);
        }

        if (StringUtil.isBlank(sourceDoc.getMetadata())) {
            log.info("文档-{}-元数据为空无需更新", sourceDoc.getName());
            return;
        }
        // 更新现在的
        updateDocMetadataRequest.setDeletes(null);
        Map<String, Object> metadata = JSONObject.parseObject(sourceDoc.getMetadata(), new TypeReference<Map<String, Object>>(){});

        List<MetadataUpdate> metadataUpdates = metadata.entrySet().stream().map(entry -> {
            MetadataUpdate metadataUpdate = new MetadataUpdate();
            metadataUpdate.setKey(entry.getKey());
            metadataUpdate.setValue(entry.getValue());
            return metadataUpdate;
        }).collect(Collectors.toList());
        updateDocMetadataRequest.setUpdates(metadataUpdates);
        ragFlowIntegrationService.updateDocumentMetadata(updateDocMetadataRequest);
    }


    private void saveDocument(DocumentDO documentDO) {
        documentDO.setUpdateTime(new Date());
        documentDO.setTaskStatus("processing");
        documentMapper.updateByPrimaryKeySelective(documentDO);
    }


    private boolean deleteRagDocument(String vecId, String datasetId) {
        RagFlowResponse<DeleteDocumentResponse> res = new RagFlowResponse<>();
        if (StringUtil.isNotBlank(vecId) && StringUtil.isNotBlank(datasetId)) {
            res = ragFlowIntegrationService.deleteRagDocument(vecId, datasetId);
        }
        if (ObjectUtils.isEmpty(res) || !RagFlowConstant.HTTP_SUCCESS.equals(res.getCode())) {
            return false;
        }
        return true;
    }

    private boolean deleteRagDataset(String datasetId) {
        RagFlowResponse<DeleteDatasetResponse> res = new RagFlowResponse<>();
        if (StringUtil.isNotBlank(datasetId) && StringUtil.isNotBlank(datasetId)) {
            res = ragFlowIntegrationService.deleteRagDataset(datasetId);
        }
        if (ObjectUtils.isEmpty(res) || !RagFlowConstant.HTTP_SUCCESS.equals(res.getCode())) {
            return false;
        }
        return true;
    }


    /**
     * 上传文件
     * @param documentDO 请求参数
     * @return UploadDocumentResponse
     */
    private UploadDocumentResponse uploadDocument2RagFlow(DocumentDO documentDO) {
        UploadDocumentRequest updateDocumentRequest = new UploadDocumentRequest();
        updateDocumentRequest.setDatasetId(documentDO.getDatasetId());
        updateDocumentRequest.setFilePath(documentDO.getBucketPath());
        // ragflow 文档名存在长度限制，仅截断上传给 ragflow 的文件名（保留扩展名）；
        // document 库中仍保存完整名称，供前端列表与检索参考文献拼接展示使用
        updateDocumentRequest.setFileName(truncateFileNameForRagFlow(documentDO.getName()));
        updateDocumentRequest.setBucketName(documentDO.getBucketName());
        return ragFlowIntegrationService.uploadDocument(updateDocumentRequest);
    }

    /**
     * 截断上传给 ragflow 的文档名，规避 ragflow 名称长度限制。
     * 规则：保留文件扩展名（ragflow 依赖后缀选择解析器，不可截断），
     * 仅对主名部分超长时截断并追加「…」；数据库存储的完整名称不受影响。
     *
     * @param fileName 原始完整文件名
     * @return 截断后的文件名（主名 ≤ 200 字符 + 省略号 + 原扩展名）
     */
    private String truncateFileNameForRagFlow(String fileName) {
        if (StringUtil.isBlank(fileName)) {
            return fileName;
        }
        int dotIndex = fileName.lastIndexOf('.');
        // 无扩展名（或以「.」开头的隐藏文件，如 .gitignore）：整体视为主名
        String mainName = (dotIndex <= 0) ? fileName : fileName.substring(0, dotIndex);
        String extension = (dotIndex <= 0) ? "" : fileName.substring(dotIndex);
        if (mainName.length() <= RAGFLOW_FILE_NAME_MAX_LENGTH) {
            return fileName;
        }
        return mainName.substring(0, RAGFLOW_FILE_NAME_MAX_LENGTH) + "…" + extension;
    }



    /**
     * 文档分片
     * @param documentDO  文档
     * @param ragFlowPipelineDTO  请求参数
     */
    private void chunkDocument2RagFlow(DocumentDO documentDO, RagFlowPipelineDTO ragFlowPipelineDTO) {
        if (!ragFlowPipelineDTO.getVectorEnable()) {
            log.info("ragFlow文档{}不进行分片， 数据集:{}", documentDO.getDocId(), documentDO.getDatasetId());
            return;
        }
        ChunkDocumentRequest chunkDocumentRequest = new ChunkDocumentRequest();
        chunkDocumentRequest.setDatasetId(documentDO.getDatasetId());
        chunkDocumentRequest.setDocumentIds(Collections.singletonList(documentDO.getVectorId()));
        ragFlowIntegrationService.chunkDocument(chunkDocumentRequest);
    }

    /**
     * 更新文档信息
     * @param documentDO 请求参数
     */
    private void updateDocument2RagFlow(DocumentDO documentDO) {
        if (StringUtil.isBlank(documentDO.getVectorId()) || StringUtil.isBlank(documentDO.getDatasetId())) {
            log.info("文档无向量Id或数据集id， 无法更新信息");
            return;
        }
        UpdateDocumentRequest updateDocumentRequest = new UpdateDocumentRequest();
        updateDocumentRequest.setDatasetId(documentDO.getDatasetId());
        updateDocumentRequest.setDocumentId(documentDO.getVectorId());
        if (StringUtil.isNotBlank(documentDO.getMetadata())) {
            updateDocumentRequest.setMetaFields(JSON.parseObject(documentDO.getMetadata(), new TypeReference<Map<String, Object>>(){}));
        }
        ragFlowIntegrationService.updateDocument(updateDocumentRequest);
    }

    @Override
    public boolean submitVectorTask(String id, String operatorType) {
        log.info("提交向量化任务。文档ID：{}", id);
        DocumentDO documentDO = documentMapper.selectByDocId(id);
        DocTypeDO docTypeDO = docTypeMapper.getDocTypeListByDocId(id).get(0);
        DocumentAddIn documentAddIn = new DocumentAddIn();
        documentAddIn.setDocId(documentDO.getDocId());
        documentAddIn.setDocName(documentDO.getName());
        documentAddIn.setChannel(documentDO.getChannel());
        documentAddIn.setMetadata(documentDO.getMetadata());
        documentAddIn.setBackName(documentDO.getBucketName());
        documentAddIn.setAwsFilePath(documentDO.getBucketPath());
        documentAddIn.setDocOperatorType(operatorType);
        documentAddIn.setDataset(docTypeDO.getDatasetId());

        try {
            return aidpDocumentService.extractedLocal(documentAddIn);
        } catch (BusinessException businessException) {
            log.error("文档加入RagFlow向量数据库失败", businessException);
            return false;
        }
    }

    @Override
    public void updateProcessingDocumentsStatus(List<DocumentDO> documentDOList){
        List<DocumentDO> processingDoc = documentDOList.stream()
                .filter(doc -> "processing".equals(doc.getTaskStatus()))
                .collect(Collectors.toList());
        if(processingDoc.isEmpty()){
            return;
        }

        Set<String> docTypes = processingDoc.stream()
                .filter(doc -> "processing".equals(doc.getTaskStatus()) && doc.getDocType() != null)
                .map(DocumentDO::getDocType)
                .collect(Collectors.toSet());

        Map<String,String> datasetIdCache = new HashMap<>();
        for (String docType: docTypes){
            try {
                DocTypeDO docTypeDO = docTypeMapper.selectByCode(docType);
                if (docTypeDO != null && docTypeDO.getDatasetId() != null) {
                    datasetIdCache.put(docType, docTypeDO.getDatasetId());
                }
            }catch (Exception e){
                log.warn("获取文档类型datasetId失败,docType：{},错误：{}",docType,e.getMessage());
            }
        }

        //并发调用RagFlow API查询状态
        Map<Long,String> statusResults = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (DocumentDO doc: processingDoc){
            String vectorId  = doc.getVectorId();
            if(vectorId != null && !vectorId.isEmpty()){
                String datasetId = datasetIdCache.getOrDefault(doc.getDocType(),null);
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        RagFlowDocumentListDetail ragDocumentDetailList = ragFlowIntegrationService.getRagDocumentDetail(vectorId, datasetId);
                        if (ragDocumentDetailList != null && ragDocumentDetailList.getDocs() != null && ragDocumentDetailList.getDocs().size()>0) {
                            String status = ragDocumentDetailList.getDocs().get(0).getRun();
                            statusResults.put(doc.getId(), status);
                        }
                    }catch(Exception e){
                        log.warn("查询文档{}状态失败,错误：{}",vectorId,e.getMessage());
                    }
                });
                future.exceptionally(ex -> {
                    log.warn("异步查询文档{}状态失败,错误：{}",vectorId,ex.getMessage());
                    return null;
                });
                futures.add(future);
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        for (DocumentDO doc: documentDOList){
            if("processing".equals(doc.getTaskStatus())) {
                // 与原逻辑一致：本轮没有任何查询结果时按 UNSTART 兜底
                String runStatus = statusResults.isEmpty() ? "0" : statusResults.get(doc.getId());
                if(runStatus == null){
                    doc.setTaskStatus("pending");
                    break;
                }
                switch (runStatus) {
                    case "1":
                    case "RUNNING":
                        doc.setTaskStatus("processing");
                        break;
                    case "2":
                    case "DONE":
                        doc.setTaskStatus("success");
                        break;
                    case "3":
                    case "FAIL":
                        doc.setTaskStatus("failed");
                        break;
                    case "0":
                    case "UNSTART":
                    default:
                        doc.setTaskStatus("pending");
                }
            }
        }
        try {
            documentMapper.batchUpdateTaskStatus(documentDOList);
        } catch (Exception e) {
            log.error("更新文档状态失败", e);
        }
    }

    @Override
    public String getDocumentRunStatus(String vectorId, String datasetId) {
        if (StringUtil.isBlank(vectorId) || StringUtil.isBlank(datasetId)) {
            return null;
        }
        RagFlowDocumentListDetail ragDocumentDetailList = ragFlowIntegrationService.getRagDocumentDetail(vectorId, datasetId);
        if (ragDocumentDetailList != null && ragDocumentDetailList.getDocs() != null && !ragDocumentDetailList.getDocs().isEmpty()) {
            return ragDocumentDetailList.getDocs().get(0).getRun();
        }
        return null;
    }

    /*@Override
    public void updateProcessingDocumentsStatus(List<DocumentDO> documentDOList){
        List<DocumentDO> processingDoc = documentDOList.stream()
                .filter(doc -> "processing".equals(doc.getTaskStatus()))
                .collect(Collectors.toList());
        if(processingDoc.isEmpty()){
            return;
        }

        Set<String> docTypes = processingDoc.stream()
                .filter(doc -> "processing".equals(doc.getTaskStatus()) && doc.getDocType() != null)
                .map(DocumentDO::getDocType)
                .collect(Collectors.toSet());

        Map<String,String> datasetIdCache = new HashMap<>();
        for (String docType: docTypes){
            try {
                DocTypeDO docTypeDO = docTypeMapper.selectByCode(docType);
                if (docTypeDO != null && docTypeDO.getDatasetId() != null) {
                    datasetIdCache.put(docType, docTypeDO.getDatasetId());
                }
            }catch (Exception e){
                log.warn("获取文档类型datasetId失败,docType：{},错误：{}",docType,e.getMessage());
            }
        }

        //并发调用RagFlow API查询状态
        Map<Long,String> statusResults = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (DocumentDO doc: processingDoc){
            String vectorId  = doc.getVectorId();
            if(vectorId != null && !vectorId.isEmpty()){
                String datasetId = datasetIdCache.getOrDefault(doc.getDocType(),null);
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        RagFlowDocumentListDetail ragDocumentDetailList = ragFlowIntegrationService.getRagDocumentDetail(vectorId, datasetId);
                        if (ragDocumentDetailList != null && ragDocumentDetailList.getDocs() == null && !ragDocumentDetailList.getDocs().isEmpty()) {
                            String status = ragDocumentDetailList.getDocs().get(0).getRun();
                            statusResults.put(doc.getId(), status);
                        }
                    }catch(Exception e){
                        log.warn("查询文档{}状态失败,错误：{}",vectorId,e.getMessage());
                    }
                });
                futures.add(future);
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        for (DocumentDO doc: documentDOList){
            if("processing".equals(doc.getTaskStatus())){
                try {
                    String vectorId  = doc.getVectorId();
                    String datasetId = datasetIdCache.getOrDefault(doc.getDocType(),null);
                    if(vectorId != null && !vectorId.isEmpty()){
                        RagFlowDocumentListDetail ragDocumentDetailList = ragFlowIntegrationService.getRagDocumentDetail(vectorId, datasetId);
                        if(ragDocumentDetailList == null || ragDocumentDetailList.getDocs() == null || ragDocumentDetailList.getDocs().isEmpty()){
                            doc.setTaskStatus("pending");
                            continue;
                        }
                        RagFlowDocumentDetail ragDocumentDetail = ragFlowIntegrationService.getRagDocumentDetail(vectorId, datasetId).getDocs().get(0);
                        if(ragDocumentDetail != null) {
                            String runStatus = ragDocumentDetail.getRun();
                            switch (runStatus) {
                                case "1":
                                case "RUNNING":
                                    doc.setTaskStatus("processing");
                                    break;
                                case "2":
                                case "DONE":
                                    doc.setTaskStatus("success");
                                    break;
                                case "3":
                                case "FAIL":
                                    doc.setTaskStatus("failed");
                                    break;
                                case "0":
                                case "UNSTART":
                                default:
                                    doc.setTaskStatus("pending");
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        try {
            documentMapper.batchUpdateTaskStatus(documentDOList);
        } catch (Exception e) {
            log.error("更新文档状态失败", e);
        }
    }*/
}
