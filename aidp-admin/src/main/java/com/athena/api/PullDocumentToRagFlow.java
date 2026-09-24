package com.athena.api;

import com.athena.api.out.PersonalDocSyncRes;

/**
 * zsk（企业知识库）文档推送 Dubbo 接口（本地契约副本，原属 athena-api 模块）。
 * <p>
 * 说明：
 * 1. AIDP 作为消费方调用该接口，触发 zsk 侧的「拉取 → 推送」流程；
 * 2. zsk 拉取到文档后，通过 {@code com.zzccaidp.AidpDocumentService#addDocument} 反向回调 AIDP 落库；
 * 3. 与 {@code com.zzccaidp.AidpDocumentService} 一样，本文件仅为本地接口副本，避免跨仓编译依赖，
 * 方法签名必须与 zsk 侧保持一致。
 */
public interface PullDocumentToRagFlow {

    /**
     * 同步指定用户的个人知识库文档到 AIDP。
     * <p>
     * AIDP 在调用前会先通过 RAGFlow 懒创建/复用该用户的个人 dataset，
     * 并把 datasetId 透传给 zsk；zsk 推送文档时原样带回（写入 {@code DocumentAddIn.dataset}），
     * AIDP 落库时直接写入 {@code document.own_dataset_id}，
     * 从而免去「靠 metadata/permission 反查工号再决定往哪个库推」的检索逻辑。
     *
     * @param userName  用户工号（zsk 与 AIDP 唯一一致的用户标识）
     * @param datasetId AIDP 侧为个人知识库分配的 RAGFlow 数据集ID
     * @return 同步结果（是否成功、真实文档数、失败原因）；调用方必须据此写回同步状态，
     *         不可再按「无异常即成功」处理
     */
    PersonalDocSyncRes pullPersonalDocument(String userName, String datasetId);

    /**
     * 按指定起点时间重放（重新同步）个人知识库文档到 AIDP（本地契约副本，与 zsk 侧 athena-api 保持一致）。
     * <p>
     * 与 {@link #pullPersonalDocument(String, String)} 的差异：
     * <ul>
     *     <li>查询起点为调用方指定的 startTime（回拨 1 秒）而非内部水位，
     *         用于恢复「AIDP 侧记录被删除/缺失但 zsk 侧无新变更」的文档；</li>
     *     <li>重放不推进增量水位：下次普通同步仍按原水位执行（update 幂等，重叠重推无害）；</li>
     *     <li>删除镜像（ai_sync_status=0）照常推送，与普通同步一致。</li>
     * </ul>
     * 兼容性：新增方法不影响既有调用方，但要求 zsk（provider）先于 AIDP（consumer）发版，
     * 否则会出现「consumer 调用新方法 → provider 无此方法」的调用失败。
     *
     * @param userName  用户工号（zsk 与 AIDP 唯一一致的用户标识）
     * @param datasetId AIDP 侧为个人知识库分配的 RAGFlow 数据集ID
     * @param startTime 重放起点时间（yyyy-MM-dd HH:mm:ss，不得晚于当前时间）
     * @return 同步结果（是否成功、真实文档数、失败原因）
     */
    PersonalDocSyncRes pullPersonalDocumentFrom(String userName, String datasetId, String startTime);

    /**
     * 查询指定用户个人知识库的最近一次增量同步水位时间（本地契约副本，与 zsk 侧保持一致）。
     * <p>
     * 供 AIDP 前端展示「上次同步时间」，并作为「重新同步」起点的默认值。
     *
     * @param userName 用户工号
     * @return 上次同步水位时间（yyyy-MM-dd HH:mm:ss）；从未同步过返回 null
     */
    String getLastPersonalSyncTime(String userName);
}
