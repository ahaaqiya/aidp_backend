package com.zzccaidp.vo.ai;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author zhangtiantian
 * @date 2026/5/8
 */
@Data
public class FXDocsAnalyzingReq {
    /**
     * 待解析文件
     */
    private List<MultipartFile> wfile;

    /**
     * 字段映射集文件
     */
    @Deprecated
    private MultipartFile template;

    /**
     * 提示词文本文件
     */
    @Deprecated
    private MultipartFile prompt;


    private String promptJsonStr;

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

}
