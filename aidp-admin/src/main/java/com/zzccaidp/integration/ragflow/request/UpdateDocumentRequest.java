package com.zzccaidp.integration.ragflow.request;

import com.alibaba.fastjson.parser.ParserConfig;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.Map;

/**
 * @author zhangtiantian
 * @date 2026/3/24
 */
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class UpdateDocumentRequest {

    private String datasetId;

    private String documentId;

    private ParserConfig parserConfig;

    private Map<String, Object> metaFields;

    /**
     * 文档级切法（ragflow chunk_method）。为空不下发（维持 dataset 建库默认切法）。
     * <p>
     * 背景：ragflow v0.27.2 上传接口对表单 parser_config 有白名单（chunk_method 被静默丢弃），
     * 文档级切法唯一有效途径就是本 update document 接口的 chunk_method 字段
     * （服务端 update_chunk_method 切 parser_id 并 reset 重解析），须在触发分块之前设置。
     * 与 Python 端 RagFlowIngestClient.setDocument_chunk_method 同一机制，两端保持一致。
     */
    private String chunkMethod;
}
