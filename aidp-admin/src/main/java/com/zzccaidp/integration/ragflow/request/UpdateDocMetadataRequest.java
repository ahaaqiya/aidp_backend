package com.zzccaidp.integration.ragflow.request;

import com.zzccaidp.integration.ragflow.dto.MetadataDelete;
import com.zzccaidp.integration.ragflow.dto.MetadataUpdate;
import com.zzccaidp.integration.ragflow.dto.UpdateMetadataSelector;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

/**
 * @author zhangtiantian
 * @date 2026/5/18
 */
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class UpdateDocMetadataRequest {

    private UpdateMetadataSelector selector;

    private List<MetadataUpdate> updates;

    private List<MetadataDelete> deletes;

    private String datasetId;
}
