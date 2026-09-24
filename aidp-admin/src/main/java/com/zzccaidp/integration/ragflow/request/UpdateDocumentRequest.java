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
}
