package com.zzccaidp.integration.ragflow.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

/**
 * @author zhangtiantian
 * @date 2026/3/24
 */
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ChunkDocumentRequest {

    private String datasetId;

    private List<String> documentIds;
}
