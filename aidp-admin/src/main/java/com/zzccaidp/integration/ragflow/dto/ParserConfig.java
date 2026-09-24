package com.zzccaidp.integration.ragflow.dto;

import lombok.Data;

import java.util.List;

/**
 * @author zhangtiantian
 * @date 2026/3/24
 */
@Data
public class ParserConfig {

    private Integer autoKeywords;

    private Integer autoQuestions;

    private Integer chunkTokenNum;

    private String delimiter;

    private Boolean html4excel;

    private String layoutRecognize;

    private List<String> tagKbIds;

    private Integer taskPageSize;

    private Raptor raptor;

    private Graphrag graphrag;
}
