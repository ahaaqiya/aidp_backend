package com.athena.api.out;

import java.io.Serializable;

/**
 * 个人知识库同步结果（zsk → AIDP 的 Dubbo 返回值）。
 * <p>
 * 背景：原接口 {@code pullPersonalDocument} 为 void，zsk 侧「用户工号为空 / 用户不存在 /
 * 未抢到并发锁 / 内部异常被吞掉」等逻辑失败都不抛异常，AIDP 只能按「无异常即成功」处理，
 * 导致前端显示同步完成但实际一条都未同步（假成功）。改为返回本对象后，
 * AIDP 可据实写回同步状态与真实文档数。
 * <p>
 * 注意：本类为 zsk 侧 {@code athena-api} 出参的<b>本地契约副本</b>，避免跨仓编译依赖；
 * 包路径、字段名与字段类型必须与 zsk 侧（{@code com.athena.api.out.PersonalDocSyncRes}）
 * 完全一致，两侧改动需同步。
 * <p>
 * 注意：本类为 Dubbo 传输对象，必须可序列化。
 *
 * @author trae
 */
public class PersonalDocSyncRes implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 整体是否同步成功：true-本轮无失败；false-存在失败（原因见 message） */
    private boolean success;

    /** 本轮扫描到的待推送文档总数（新增/更新 + 删除） */
    private int total;

    /** 成功推送到 AIDP 的文档数 */
    private int successCount;

    /** 结果描述：成功为提示语，失败为具体原因（供 AIDP 透传前端展示） */
    private String message;

    // ==================== 手写 getter / setter ====================
    // 变更说明：原使用 lombok @Data 自动生成访问器，现改为手写形式（去除 lombok 依赖）。
    // 方法名与 lombok 生成结果保持完全一致（boolean 字段为 isSuccess），
    // 避免既有调用方（AIDP 侧 PersonalDocumentService#doSync 使用 isSuccess / getTotal /
    // getSuccessCount / getMessage）编译失败；zsk 侧契约副本需同步修改。

    /**
     * 获取整体是否同步成功。
     *
     * @return true-本轮无失败；false-存在失败（原因见 message）
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 设置整体是否同步成功。
     *
     * @param success 是否同步成功
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * 获取本轮扫描到的待推送文档总数。
     *
     * @return 待推送文档总数（新增/更新 + 删除）
     */
    public int getTotal() {
        return total;
    }

    /**
     * 设置本轮扫描到的待推送文档总数。
     *
     * @param total 待推送文档总数
     */
    public void setTotal(int total) {
        this.total = total;
    }

    /**
     * 获取成功推送到 AIDP 的文档数。
     *
     * @return 成功推送文档数
     */
    public int getSuccessCount() {
        return successCount;
    }

    /**
     * 设置成功推送到 AIDP 的文档数。
     *
     * @param successCount 成功推送文档数
     */
    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    /**
     * 获取结果描述。
     *
     * @return 成功为提示语，失败为具体原因
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置结果描述。
     *
     * @param message 结果描述（供 AIDP 透传前端展示）
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 构造成功结果。
     *
     * @param total        本轮待推送文档总数
     * @param successCount 成功推送文档数
     * @param message      结果提示语
     * @return 成功结果对象
     */
    public static PersonalDocSyncRes ok(int total, int successCount, String message) {
        return build(true, total, successCount, message);
    }

    /**
     * 构造失败结果。
     *
     * @param total        本轮待推送文档总数（未知时传 0）
     * @param successCount 成功推送文档数（未知时传 0）
     * @param message      失败原因
     * @return 失败结果对象
     */
    public static PersonalDocSyncRes fail(int total, int successCount, String message) {
        return build(false, total, successCount, message);
    }

    private static PersonalDocSyncRes build(boolean success, int total, int successCount, String message) {
        PersonalDocSyncRes res = new PersonalDocSyncRes();
        res.setSuccess(success);
        res.setTotal(total);
        res.setSuccessCount(successCount);
        res.setMessage(message);
        return res;
    }
}
