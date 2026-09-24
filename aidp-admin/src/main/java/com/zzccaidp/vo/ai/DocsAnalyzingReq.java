package com.zzccaidp.vo.ai;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author liuxiazhang
 * @date 2025/8/25
 */
public class DocsAnalyzingReq {

    /**
     * 待解析文件
     */
    private MultipartFile wfile;

    /**
     * 字段映射集文件
     */
    private MultipartFile template;

    /**
     * 提示词文本文件
     */
    private MultipartFile prompt;

    /**
     * 场景（fast：快思考，deep：慢思考）
     */
    private String scene;

    /**
     * 提取模式（1：统一提取；2：提取模式）
     */
    private String mode;

    /**
     * 识别方式（1：标准文档识别；2：表格文档识别）
     */
    private String recognizeType;

    /**
     * 指定文档页码
     */
    private String page;

    /**
     * 用户标识
     */
    private String user;

    public MultipartFile getWfile() {
        return wfile;
    }

    public void setWfile(MultipartFile wfile) {
        this.wfile = wfile;
    }

    public MultipartFile getTemplate() {
        return template;
    }

    public void setTemplate(MultipartFile template) {
        this.template = template;
    }

    public MultipartFile getPrompt() {
        return prompt;
    }

    public void setPrompt(MultipartFile prompt) {
        this.prompt = prompt;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getRecognizeType() {
        return recognizeType;
    }

    public void setRecognizeType(String recognizeType) {
        this.recognizeType = recognizeType;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
