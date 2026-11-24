package com.zzccaidp.out;

import lombok.Data;

import java.io.Serializable;

/**
 * 向量化任务消息（Redis List 队列元素，Java 投递、Python 端消费）。
 *
 * <p>字段与 Python 端 VectorizeTask 一一对应（JSON 序列化后 camelCase 对齐）。</p>
 */
@Data
public class VectorizeTaskMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 文档业务ID */
    private String docId;

    /** 文档名称 */
    private String docName;

    /** 渠道标识 */
    private String channel;

    /** 操作类型：add / update */
    private String docOperatorType;

    /** AIDP 对象存储桶名 */
    private String bucketName;

    /** 桶内文件路径 */
    private String bucketPath;

    /** 文档元数据（JSON 字符串） */
    private String metadata;

    /** 旧向量ID（update 时用于先删旧向量） */
    private String oldVectorId;

    /** 是否跳过 Python 大模型分类（true=沿用 Java 落库 doc_type，如 personal/oicc 渠道） */
    private Boolean skipClassify;

    /** 重试次数（投递时固定为 0，Python 端消费重试时自增） */
    private Integer retryCount;

    /** 知识库类型 */
    private String docType;
}
