package com.zzccaidp.enums;

import lombok.Getter;

/**
 * 资源库切片方式字典（与 ragflow dataset 的 chunk_method 取值对齐）。
 * <p>
 * 作用：
 * 1. 作为创建/编辑资源库时「切片方式」下拉的统一数据源（GET /doc_type/chunk_methods）；
 * 2. 对入参 chunkMethod 做合法性校验，非法值在本地拦截，避免透传 ragflow 被 pydantic 拒绝；
 * 3. 创建资源库联动创建 ragflow dataset 时透传 chunk_method，使选择真正生效。
 * <p>
 * 仅开放文本文档场景的常用子集（picture/audio/email/tag/knowledge_graph 等不适用场景未开放）。
 */
@Getter
public enum ChunkMethodEnum {

    /** 通用分片（默认，适用于大多数文本文档） */
    NAIVE("naive", "通用"),

    /** 问答对分片（QA 逐对切分） */
    QA("qa", "问答"),

    /** 手册分片（按章节层级切分） */
    MANUAL("manual", "手册"),

    /** 表格分片（按表格行切分） */
    TABLE("table", "表格"),

    /** 论文分片（按章节切分） */
    PAPER("paper", "论文"),

    /** 书籍分片（按章节切分） */
    BOOK("book", "书籍"),

    /** 法律条文分片（按条款切分） */
    LAWS("laws", "法律"),

    /** 演示文稿分片（按页切分） */
    PRESENTATION("presentation", "演示文稿"),

    /** 整篇单块（不切分，全文一个 chunk） */
    ONE("one", "整篇");

    /** ragflow chunk_method 取值 */
    private final String value;

    /** 中文展示名 */
    private final String label;

    ChunkMethodEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * 校验取值是否在开放字典内
     *
     * @param value 切片方式取值
     * @return true-合法
     */
    public static boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        for (ChunkMethodEnum method : values()) {
            if (method.value.equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 按取值查找枚举
     *
     * @param value 切片方式取值
     * @return 对应枚举，未匹配返回 null
     */
    public static ChunkMethodEnum of(String value) {
        for (ChunkMethodEnum method : values()) {
            if (method.value.equals(value)) {
                return method;
            }
        }
        return null;
    }
}
