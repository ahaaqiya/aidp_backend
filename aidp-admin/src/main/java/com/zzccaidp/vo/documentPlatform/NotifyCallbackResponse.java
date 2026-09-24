package com.zzccaidp.vo.documentPlatform;

import lombok.Data;

/**
 * @author zhangtiantian
 * @date 2026/2/10
 */
@Data
public class NotifyCallbackResponse {
    private Integer code;
    private String message;
    private String details;
    private String hint;

    public NotifyCallbackResponse(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
