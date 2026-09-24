package com.zzccaidp.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author zhangtiantian
 * @date 2026/5/8
 */
@Data
public class FXNewDocsAnalyzingApiDTO {
    /**
     * 实例
     */
    private FXNewDocsAnalyzingApiInputDTO inputs;

    /**
     * 返回方式
     */
    @JsonProperty("response_mode")
    private String responseMode;

    /**
     * 用户标识
     */
    private String user;
}
