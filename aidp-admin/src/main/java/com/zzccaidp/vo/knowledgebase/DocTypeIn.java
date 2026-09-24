package com.zzccaidp.vo.knowledgebase;

import lombok.Data;

@Data
public class DocTypeIn {
    private String name;
    private String code;
    private String description;
    private String chunkMethod;
    private String datasetId;
    /** 是否参与知识库问答检索：1-参与，0-不参与（null 时不更新，新建靠数据库默认值 1） */
    private Integer retrievalEnabled;
}