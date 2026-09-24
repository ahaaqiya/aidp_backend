package com.zzccaidp.in;

import lombok.Data;

import java.io.Serializable;

/**
 * 文档推送入参（原属 zzccimp-aid-api 模块）
 */
@Data
public class DocumentAddIn implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 文档ID */
    private String docId;

    /** 渠道 */
    private String channel;

    /** 操作类型（add/update/delete） */
    private String docOperatorType;

    /** 文档名称 */
    private String docName;

    /** 桶名称 */
    private String backName;

    /** S3 桶内文件路径 */
    private String awsFilePath;

    /** 元数据（JSON 字符串） */
    private String metadata;

    /** RagFlow 数据集ID */
    private String dataset;

    /** 所属正文标题（附件时填写，正文为空） */
    private String parentDocName;

    /** 是否附件：0-正文，1-附件 */
    private Integer isAttachment;
}
