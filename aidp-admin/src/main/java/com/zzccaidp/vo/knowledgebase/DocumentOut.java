package com.zzccaidp.vo.knowledgebase;

import lombok.Data;

import java.util.Date;

@Data
public class DocumentOut {
    private Long id;
    private String docId;
    private Long dataSourceId;
    private String channel;
    private String name;
    private String fileType;
    private String docType;
    private String docTypeName;
    private String sourceName;
    private String sourceStatus;
    private Boolean switchStatus;
    private String taskStatus;
    private Date lastProcessTime;
    private String md5;
    private String fileSize;
    private String creator;
    private Date createTime;
    private String modifier;
    private Date updateTime;
    private String visibility;
    private String xskyPath;
    private String bucketName;
    private String bucketPath;
    private String vectorEnabled;
    private String vectorId;
    private String metadata;
    private String status;
    /** 所属正文标题（附件时返回，正文为空） */
    private String parentDocName;
    /** 是否附件：0-正文，1-附件 */
    private Integer isAttachment;
}
