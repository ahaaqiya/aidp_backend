package com.zzccaidp.vo.knowledgebase;

import lombok.Data;

/**
 * 个人知识库同步状态（存于 Redis 临时状态，非持久化）。
 * <p>
 * 前端轮询 {@code GET /personalDocument/syncStatus} 时返回该结构，
 * 用于展示「同步中(进度)/完成/失败」。
 *
 * @author trae
 */
@Data
public class PersonalSyncStatus {

    /** 同步状态：running-进行中 / success-完成 / failed-失败 */
    private String status;

    /** 已处理文档数 */
    private Integer processed;

    /** 待处理文档总数 */
    private Integer total;

    /** 结果消息（成功提示或失败原因） */
    private String message;

    /** 最近一次同步时间（yyyy-MM-dd HH:mm:ss） */
    private String lastSyncTime;
}
