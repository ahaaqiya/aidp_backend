package com.zzccaidp.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author liuxiazhang
 * @date 2025/8/26
 */
public class DocsAnalyzingDTO {

    /**
     * 实例
     */
    private DocsAnalyzingInputDTO inputs;

    /**
     * 返回方式
     */
    @JsonProperty("response_mode")
    private String responseMode;

    /**
     * 用户标识
     */
    private String user;

    public DocsAnalyzingInputDTO getInputs() {
        return inputs;
    }

    public void setInputs(DocsAnalyzingInputDTO inputs) {
        this.inputs = inputs;
    }

    public String getResponseMode() {
        return responseMode;
    }

    public void setResponseMode(String responseMode) {
        this.responseMode = responseMode;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
