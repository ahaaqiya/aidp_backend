package com.zzccaidp.dto.ai;

import lombok.Data;

/**
 * @author liuxiazhang
 * @date 2025/8/26
 */
@Data
public class NewDocsAnalyzingInputDTO {
    //文档内容
    private String wfileText;

    //场景类型标记
    private String type;

    //提示词
    private String prompt;

    //提取模式（1-统一提取,2-逐页提取）
    private String mode;

    //文本内容列表
    private String wfileTextList;

    //场景（fast：快思考，deep：慢思考）
    private String scene;


}
