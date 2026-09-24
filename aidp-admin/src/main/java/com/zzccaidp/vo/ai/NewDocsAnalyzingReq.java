package com.zzccaidp.vo.ai;

import lombok.Data;

/**
 * @author liuxiazhang
 * @date 2025/8/25
 */
@Data
public class NewDocsAnalyzingReq {

    /**
     * 待解析文件
     */
    private String fileContent;


    /**
     * 提示词文本文件
     */
    private String prompt;

    /**
     * 场景（fast：快思考，deep：慢思考）
     */
    private String scene;

    /**
     * 提取模式（1：统一提取；2：提取模式）
     */
    private String mode;

    /**
     * 场景类型标记
     */
    private String type;

    /**
     * 指定文档页码
     */
    private String page;

    /**
     * 用户标识
     */
    private String user;

    //文本内容列表
    private String wfileTextList;



}
