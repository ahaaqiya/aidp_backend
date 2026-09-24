package com.zzccaidp.dto.ai;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author zhangtiantian
 * @date 2026/5/8
 */
@Data
public class FXNewDocsAnalyzingApiInputDTO {
    private static final long serialVersionUID = 1L;

    /**
     * 场景（fast：快思考，deep：慢思考）
     */
    private String scene;


    private List<Map<String, String>> wfile;

    private Map<String, String> template;

    private Map<String, String> prompt;

    private String promptJsonStr;

    private String mode;

    private String page;


    private String recognizeType;
}
