package com.zzccaidp.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author liuxiazhang
 * @date 2025/8/26
 */
@Data
public class NewDocsAnalyzingDTO {

    /**
     * 实例
     */
    private NewDocsAnalyzingInputDTO inputs;

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
