package com.zzccaidp.service.knowledgebase;

import cn.hutool.core.bean.BeanUtil;
import com.zzccaidp.common.AmazonS3Util;
import com.zzccaidp.dao.knowledgebase.DocTypeDO;
import com.zzccaidp.dao.knowledgebase.DocumentDO;
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
    private AmazonS3Util amazonS3Util;

    @Value("${bades.file.aws-s3.bucketName}")
    private String backName;

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

    @Transactional
    public DocTypeDO create(DocTypeIn request) {
        DocTypeDO docType = new DocTypeDO();
        docType.setId(UUID.randomUUID().toString());
        docType.setName(request.getName());
        docType.setCode(request.getCode());
        docType.setDescription(request.getDescription());
        docType.setCount(0);
        if (request.getChunkMethod() == null) {
            docType.setChunkMethod("auto");
        } else {
            docType.setChunkMethod(request.getChunkMethod());
        }
        docType.setSourceCount(0);
        docType.setUpdateTime(new Date());
        docType.setStatus("active");
        docTypeMapper.insert(docType);
        return docType;
    }

    @Transactional
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
        if (request.getChunkMethod() != null) {
            docType.setChunkMethod(request.getChunkMethod());
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

    @Transactional(rollbackFor = Exception.class)
    public boolean delete(String id) {
        DocTypeDO docType = docTypeMapper.selectById(id);
        if (docType == null) {
            return false;
        }
        try {
            if(docType.getDatasetId() != null && !docType.getDatasetId().isEmpty()) {
                boolean success = ragFlowService.deleteRagDataset(docType);
                if (!success) {
                    log.error("删除ragFlow数据集失败");
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return false;
                }
                docType.setDatasetId(null);
                docTypeMapper.updateByPrimaryKey(docType);
            }

            //删除关联文档
            DocumentDO queryDO = new DocumentDO();
            queryDO.setDocType(docType.getCode());
            List<DocumentDO> documentDOList = documentMapper.selectByCondition(queryDO);
            List<String> fastDfsFileIdList = documentDOList.stream().
                    map(DocumentDO::getBucketPath)
                    .collect(Collectors.toList());
            amazonS3Util.batchDeleteFile(fastDfsFileIdList,backName);
            documentMapper.batchDeleteByDocTypeId(docType.getCode());
            //删除数据集
            docTypeMapper.deleteById(id);

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