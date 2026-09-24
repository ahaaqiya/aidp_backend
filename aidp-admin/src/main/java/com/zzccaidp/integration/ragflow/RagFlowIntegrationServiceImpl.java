package com.zzccaidp.integration.ragflow;

import com.alibaba.fastjson.JSON;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.zzccaidp.common.AmazonS3Util;
import com.zzccaidp.constants.RagFlowConstant;
import com.zzccaidp.dao.knowledgebase.DocumentDO;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.exception.BusinessException;
import com.zzccaidp.exception.RagFlowException;
import com.zzccaidp.integration.ragflow.request.*;
import com.zzccaidp.integration.ragflow.response.*;
import com.zzccaidp.mapper.knowledgebase.DocumentMapper;
import com.zzccaidp.vo.knowledgebase.RagFlowDocumentListDetail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * @author zhangtiantian
 * @date 2026/3/24
 */
@Service
@Slf4j
public class RagFlowIntegrationServiceImpl implements RagFlowIntegrationService {

    @Resource(name = "ragFlowWebClient")
    private WebClient webClient;

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private AmazonS3Util amazonS3Util;

    @Value("${bades.file.aws-s3.bucketName}")
    private String bucketName;

    @Value("${ragFlow.apiKey:ragflow-zqdqdaw8tL_7ibqKWjaztU8KG5kDygdeCS5qaDHgXHk}")
    private String apiKey;

    @Autowired
    private DocumentMapper documentMapper;
    @Override
    public CreateDataSetResponse createDataSet(CreateDataSetRequest createDataSetRequest) {
        try {
            log.info("请求ragFlow创建数据集，param：{}", JSON.toJSONString(createDataSetRequest));
            RagFlowResponse<CreateDataSetResponse> res = webClient.post()
                    .uri(RagFlowConstant.CREATE_DATASET_URI)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(createDataSetRequest)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<RagFlowResponse<CreateDataSetResponse>>() {
                    })
                    .block();
            if (ObjectUtils.isEmpty(res) || !RagFlowConstant.HTTP_SUCCESS.equals(res.getCode())) {
                log.error("ragFlow创建数据集失败， 响应：{}", res);
                throw new RagFlowException(ErrCodeEnum.M7007);
            }
            return res.getData();
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("ragFlow创建数据集失败", e);
            throw new RagFlowException(ErrCodeEnum.M7007);
        }
    }

    @Override
    public UploadDocumentResponse uploadDocument(UploadDocumentRequest uploadDocumentRequest) {
        // 读取桶文件
        try (S3ObjectInputStream s3ObjectInputStream = amazonS3.getObject(uploadDocumentRequest.getBucketName(), uploadDocumentRequest.getFilePath()).getObjectContent()) {
            // 构建请求体
            byte[] bytes = IOUtils.toByteArray(s3ObjectInputStream);
            MultipartBodyBuilder body = new MultipartBodyBuilder();
            body.part("file", new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return uploadDocumentRequest.getFileName();
                }
            });
            // 调用上传文件
            log.info("请求ragFlow上传文档信息，param：{}",  JSON.toJSONString(uploadDocumentRequest));
            RagFlowResponse<List<UploadDocumentResponse>> res = webClient.post()
                    .uri(RagFlowConstant.UPLOAD_DOCUMENT_URI, uploadDocumentRequest.getDatasetId())
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body.build()))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<RagFlowResponse<List<UploadDocumentResponse>>>() {
                    })
                    .block();
            if (ObjectUtils.isEmpty(res) || !RagFlowConstant.HTTP_SUCCESS.equals(res.getCode())) {
                log.error("ragFlow上传文档信息请求失败， 响应：{}", res);
                throw new RagFlowException(ErrCodeEnum.M7002);
            }
            return res.getData().get(0);
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("ragFlow上传文档信息失败", e);
            throw new RagFlowException(ErrCodeEnum.M7002);
        }
    }

    @Override
    public ChunkDocumentResponse chunkDocument(ChunkDocumentRequest chunkDocumentRequest) {
        try {
            log.info("请求ragFlow解析文档信息，param：{}", JSON.toJSONString(chunkDocumentRequest));
            RagFlowResponse<ChunkDocumentResponse> res = webClient.post()
                    .uri(RagFlowConstant.CHUNKS_DOCUMENT_URI, chunkDocumentRequest.getDatasetId())
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(chunkDocumentRequest)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<RagFlowResponse<ChunkDocumentResponse>>() {
                    })
                    .block();
            if (ObjectUtils.isEmpty(res) || !RagFlowConstant.HTTP_SUCCESS.equals(res.getCode())) {
                log.error("ragFlow解析文档请求失败， 响应：{}", res);
                throw new RagFlowException(ErrCodeEnum.M7003);
            }
            return res.getData();
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("ragFlow解析文档失败", e);
            throw new RagFlowException(ErrCodeEnum.M7003);
        }
    }

    @Override
    public UpdateDocumentResponse updateDocument(UpdateDocumentRequest updateDocumentRequest) {
        try {
            log.info("请求ragFlow更新文档信息，param：{}", JSON.toJSONString(updateDocumentRequest));
            RagFlowResponse<UpdateDocumentResponse> res = webClient.put()
                    .uri(RagFlowConstant.UPDATE_DOCUMENT_URI, updateDocumentRequest.getDatasetId(), updateDocumentRequest.getDocumentId())
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(updateDocumentRequest)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<RagFlowResponse<UpdateDocumentResponse>>() {
                    })
                    .block();
            if (ObjectUtils.isEmpty(res) || !RagFlowConstant.HTTP_SUCCESS.equals(res.getCode())) {
                log.error("ragFlo更新文档请求失败， 响应：{}", res);
                throw new RagFlowException(ErrCodeEnum.M7001);
            }
            return res.getData();
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("ragFlow更新文档失败", e);
            throw new RagFlowException(ErrCodeEnum.M7001);
        }
    }

    @Override
    public UpdateDocMetadataResponse updateDocumentMetadata(UpdateDocMetadataRequest updateDocMetadataRequest) {
        try {
            log.info("请求ragFlow更新文档信息，param：{}", JSON.toJSONString(updateDocMetadataRequest));
            RagFlowResponse<UpdateDocMetadataResponse> res = webClient.post()
                    .uri(RagFlowConstant.UPDATE_DOCUMENT_METADATA_URI, updateDocMetadataRequest.getDatasetId())
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(updateDocMetadataRequest)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<RagFlowResponse<UpdateDocMetadataResponse>>() {
                    })
                    .block();
            if (ObjectUtils.isEmpty(res) || !RagFlowConstant.HTTP_SUCCESS.equals(res.getCode())) {
                log.error("ragFlo更新文档元数据请求失败， 响应：{}", res);
                throw new RagFlowException(ErrCodeEnum.M7006);
            }
            return res.getData();
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("ragFlo更新文档元数据请求失败", e);
            throw new RagFlowException(ErrCodeEnum.M7006);
        }
    }

    @Override
    public RagFlowResponse<DeleteDocumentResponse> deleteRagDocument(String docId, String datasetId) {
        try {
            log.info("请求ragFlow删除文档信息，docId：{}, datasetId:{}",  docId, datasetId);
            DeleteDocumentRequest deleteDocumentRequest = new DeleteDocumentRequest(Collections.singletonList(docId));
            RagFlowResponse<DeleteDocumentResponse> res = webClient.method(HttpMethod.DELETE)
                    .uri(RagFlowConstant.DELETE_DOCUMENT_URI, datasetId)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(deleteDocumentRequest)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<RagFlowResponse<DeleteDocumentResponse>>() {
                    })
                    .block();
            if (ObjectUtils.isEmpty(res) || !RagFlowConstant.HTTP_SUCCESS.equals(res.getCode())) {
                log.error("ragFlow删除文档失败， 响应：{}", res);
            }
            return res;
        } catch (Exception be) {
            log.error("ragFlow删除文档失败", be);
            return null;
        }
    }

    @Override
    public RagFlowResponse<DeleteDatasetResponse> deleteRagDataset(String datasetId) {
        try {
            log.info("请求ragFlow删除数据集，datasetId:{}",datasetId);
            DeleteDatasetRequest deleteDatasetRequest = new DeleteDatasetRequest(Collections.singletonList(datasetId));
            RagFlowResponse<DeleteDatasetResponse> res = webClient.method(HttpMethod.DELETE)
                    .uri(RagFlowConstant.DELETE_DATASET_URI)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(deleteDatasetRequest)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<RagFlowResponse<DeleteDatasetResponse>>() {
                    })
                    .block();
            if (ObjectUtils.isEmpty(res) || !RagFlowConstant.HTTP_SUCCESS.equals(res.getCode())) {
                log.error("ragFlow删除文档失败， 响应：{}", res);
            }
            return res;
        } catch (Exception be) {
            log.error("ragFlow删除文档失败", be);
            return null;
        }
    }

    @Override
    public RagFlowDocumentListDetail getRagDocumentDetail(String vectorId, String datasetId) {
        try {
            log.info("请求ragFlow查询文档信息，docId：{}, datasetId:{}",  vectorId, datasetId);
            RagFlowResponse<RagFlowDocumentListDetail> res = webClient.method(HttpMethod.GET)
                    .uri(uriBuilder -> uriBuilder
                            .path(RagFlowConstant.UPLOAD_DOCUMENT_URI)
                            .queryParam("id",vectorId)
                            .build(datasetId))
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            resp -> Mono.error(new RuntimeException("接口请求失败，HTTP状态码： " + resp.statusCode())))
                    .bodyToMono(new ParameterizedTypeReference<RagFlowResponse<RagFlowDocumentListDetail>>() {
                    })
                    .block();
            if (ObjectUtils.isEmpty(res) || !RagFlowConstant.HTTP_SUCCESS.equals(res.getCode())) {
                log.error("ragFlow查询文档详情失败， 响应：{}", res);
                return null;
            }
            return res.getData();
        } catch (Exception be) {
            log.error("ragFlow查询文档详情失败", be);
            return null;
        }
    }
}
