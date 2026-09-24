package com.zzccaidp.dto.ai;

import java.util.Map;

/**
 * @author liuxiazhang
 * @date 2025/8/26
 */
public class DocsAnalyzingInputDTO {

    private Map<String, String> wfile;

    private Map<String, String> template;

    private Map<String, String> prompt;

    private String mode;

    private String page;

    private String recognizeType;

    public Map<String, String> getWfile() {
        return wfile;
    }

    public void setWfile(Map<String, String> wfile) {
        this.wfile = wfile;
    }

    public Map<String, String> getTemplate() {
        return template;
    }

    public void setTemplate(Map<String, String> template) {
        this.template = template;
    }

    public Map<String, String> getPrompt() {
        return prompt;
    }

    public void setPrompt(Map<String, String> prompt) {
        this.prompt = prompt;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getRecognizeType() {
        return recognizeType;
    }

    public void setRecognizeType(String recognizeType) {
        this.recognizeType = recognizeType;
    }
}
