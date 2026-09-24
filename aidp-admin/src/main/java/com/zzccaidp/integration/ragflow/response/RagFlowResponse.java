package com.zzccaidp.integration.ragflow.response;

import lombok.Data;

/**
 * @author zhangtiantian
 * @date 2026/3/24
 */
@Data
public class RagFlowResponse<T> {

    private Integer code;

    private T data;

    private String message;
}
