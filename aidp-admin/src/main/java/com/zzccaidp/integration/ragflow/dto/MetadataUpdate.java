package com.zzccaidp.integration.ragflow.dto;

import lombok.Data;

/**
 * @author zhangtiantian
 * @date 2026/5/18
 */
@Data
public class MetadataUpdate {
    private String key;

    private Object value;

    private Object match;
}
