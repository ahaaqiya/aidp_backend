package com.zzccaidp.service.ragFlow.dto;

import lombok.Data;

import java.util.Map;

/**
 * @author zhangtiantian
 * @date 2026/5/8
 */
@Data
public class RagFlowPipelineDTO {

    private String docId;

    private Boolean vectorEnable = Boolean.TRUE;
}
