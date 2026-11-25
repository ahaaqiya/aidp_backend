package com.zzccaidp.enums;

import lombok.Getter;

/**
 * 资源库切片方式字典（与 agent_project llm_selector.CHUNK_METHODS 统一，取值为
 * ragflow v0.27.2 实测合法枚举的开放子集——完整枚举共 13 项：
 * naive/book/email/laws/manual/one/paper/picture/presentation/qa/table/tag/resume）。
 * <p>
 * 作用：
 * 1. 作为创建/编辑资源库时「切片方式」下拉的统一数据源（GET /doc_type/chunk_methods）；
 * 2. 对入参 chunkMethod 做合法性校验，非法值在本地拦截，避免透传 ragflow 被 pydantic 拒绝；
 * 3. 创建资源库联动创建 ragflow dataset 时透传 chunk_method，使选择真正生效。
 * <p>
 * 注意：general 不是 ragflow 值（UI 文案 General 对应内部值 naive），严禁加入本字典；
 * 文档级切法（document.shard_type）由 Python 探针侧用同一套字典判定，两端保持一致。
 */
@Getter
public enum ChunkMethodEnum {

    /** 通用分片（默认，适用于大多数文本文档） */
    NAIVE("naive", "通用"),

    /** 问答对分片（QA 逐对切分） */
    QA("qa", "问答"),

    /** 简历分片（按简历结构切分） */
    RESUME("resume", "简历"),

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
