package com.zzccaidp.vo.knowledgebase;

import lombok.Data;

import java.util.Date;

@Data
public class DocTypeOut {
    private String id;
    private String name;
    private String code;
    private String description;
    private Integer count;
    private String chunkMethod;
    private Integer sourceCount;
    private Date updateTime;
    private String sources;
    private String status;
    private String datasetId;
    /** 是否参与知识库问答检索：1-参与，0-不参与（编辑弹窗回显用） */
    private Integer retrievalEnabled;
}