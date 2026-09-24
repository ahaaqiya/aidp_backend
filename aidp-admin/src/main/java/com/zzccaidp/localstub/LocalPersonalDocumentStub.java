package com.zzccaidp.localstub;

import com.alibaba.fastjson.JSON;
import com.zzccaidp.context.UserInfoContextHolder;
import com.zzccaidp.service.knowledgebase.PersonalDocumentService;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.knowledgebase.DocumentOut;
import com.zzccaidp.vo.knowledgebase.PersonalSyncStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 【本地联调桩】个人知识库「同步」与「文档列表」的模拟实现。
 * <p>
 * <b>为什么需要这个类：</b>真实同步链路的终点是调用 zsk 提供的 Dubbo 接口
 * {@code PullDocumentToRagFlow#pullPersonalDocument}，由 zsk 侧反查后回调 AIDP 落库。
 * 本地环境 {@code dubbo.registry.address=N/A}（真实注册中心地址在 Apollo，本地不可见）且未启动 zsk provider，
 * 因此 {@code PersonalDocumentService#sync()} 提交后必然走到 catch 分支并立刻落 failed，
 * 前端只能看到「running 一闪而过 → 失败」，无法联调同步页的交互效果。
 * <p>
 * <b>启用方式（默认关闭，不传参数则本类完全不生效）：</b>
 * <pre>
 * 启动参数追加：--aidp.local-stub.personalDocument=true
 * 可选参数：
 *   --aidp.local-stub.personalDocument.duration-seconds=8  模拟「同步中」的持续时长（秒），默认 8
 *   --aidp.local-stub.personalDocument.doc-count=5         模拟返回的文档条数，默认 5
 * </pre>
 * <p>
 * <b>影响范围（仅这 3 个接口）：</b>
 * <ul>
 *   <li>POST /personalDocument/sync       —— 不调 zsk，直接写 Redis 状态 running，N 秒后自动改 success；</li>
 *   <li>GET  /personalDocument/syncStatus —— 读同一份 Redis 状态；</li>
 *   <li>GET  /personalDocument/list       —— 返回内置模拟文档（覆盖 pending/processing/success/failed 四种状态）。</li>
 * </ul>
 * <b>不影响：</b>upload / delete / process 仍走真实链路（仍会真正访问 MySQL、MinIO、ragflow）。
 * <p>
 * <b>兼容性：</b>同步状态与真实实现共用同一个 Redis key（{@code PERSONAL_SYNC_STATUS_<工号>}），
 * 关掉开关后状态可无缝回落真实链路，不会残留脏 key。
 * <p>
 * <b>⚠️ 本类仅供本地前端联调，严禁上线，联调结束后请直接删除本文件与 {@code com.zzccaidp.localstub} 包。</b>
 *
 * @author trae
 */
@Slf4j
@Service("localPersonalDocumentStub")
@Primary
@ConditionalOnProperty(name = "aidp.local-stub.personalDocument", havingValue = "true")
public class LocalPersonalDocumentStub extends PersonalDocumentService {

    /**
     * 同步状态 Redis key 前缀。
     * <p>
     * 必须与 {@code PersonalDocumentService#PERSONAL_SYNC_STATUS_KEY_PREFIX} 保持一致
     * （该常量为 private，无法直接引用，此处冗余一份；真实实现若调整前缀，本桩需同步修改）。
     */
    private static final String SYNC_STATUS_KEY_PREFIX = "PERSONAL_SYNC_STATUS_";

    /** 同步状态 key 过期时间（秒），与真实实现保持一致（24h 临时状态） */
    private static final long SYNC_STATUS_EXPIRE_SECONDS = 24 * 3600L;

    /** 模拟文档主键起始值：取一个远大于真实自增 ID 的区段，便于在页面上快速辨识是桩数据 */
    private static final long MOCK_ID_BASE = 900000L;

    /** 模拟文档业务 ID 前缀 */
    private static final String MOCK_DOC_ID_PREFIX = "MOCK-DOC-";

    /**
     * 模拟文档模板：[文件名, 文件类型, 文件大小展示值, 向量化状态]。
     * <p>
     * 四种 taskStatus 全覆盖，用于验证前端状态标签（待处理/处理中/成功/失败）的渲染与配色。
     */
    private static final String[][] MOCK_TEMPLATES = {
            {"个人工作总结-2026.docx", "docx", "1.2MB", "success"},
            {"项目周报-第36周.pdf", "pdf", "856KB", "processing"},
            {"需求评审纪要.md", "markdown", "12KB", "pending"},
            {"年度述职报告.pptx", "other", "3.4MB", "failed"},
            {"个人学习笔记.xlsx", "xlsx", "268KB", "success"},
    };

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /** 模拟「同步中」的持续时长（秒），到期后自动落 success */
    @Value("${aidp.local-stub.personalDocument.duration-seconds:8}")
    private long fakeDurationSeconds;

    /** 模拟返回的文档条数（超过模板数量时循环复用模板） */
    @Value("${aidp.local-stub.personalDocument.doc-count:5}")
    private int mockDocCount;

    /** 延迟落终态的调度线程：单线程 + 守护线程，避免阻塞应用退出 */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "local-stub-personal-sync");
        thread.setDaemon(true);
        return thread;
    });

    @PostConstruct
    public void logEnabled() {
        log.warn("[本地联调桩] 已启用：/personalDocument 的 sync、syncStatus、list 将返回模拟数据"
                + "（同步状态持续 {} 秒，模拟文档 {} 条）。本桩严禁用于生产环境！", fakeDurationSeconds, mockDocCount);
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }

    /**
     * 模拟提交同步任务：直接写 running 状态并返回，不调用 zsk Dubbo 接口。
     * <p>
     * 与真实实现的差异：真实实现用 running 兼作幂等锁（重复提交抛 M7108），
     * 本桩为方便反复联调不做拦截，重复点击会重置计时并重新开始。
     *
     * @param startTime 重新同步起点时间；桩不真正执行重放，仅在提示文案中体现该模式（空=普通同步）
     */
    @Override
    public void sync(String startTime) {
        final String userCode = currentUserCode();
        final int total = mockDocCount;
        final boolean replay = StringUtils.isNotBlank(startTime);
        writeStatus(userCode, buildStatus("running", 0, total,
                replay ? "重新同步任务已提交（自 " + startTime + " 起，本地联调桩）" : "同步任务已提交（本地联调桩）"));
        log.warn("[本地联调桩] 模拟{}已启动，userCode={}，{} 秒后自动置为 success",
                replay ? "重新同步(重放)" : "同步", userCode, fakeDurationSeconds);

        // 注意：调度线程不是请求线程，UserInfoContextHolder 的 ThreadLocal 在此不可用，
        // 因此 userCode 必须在提交任务前捕获为局部变量传入
        scheduler.schedule(() -> {
            try {
                writeStatus(userCode, buildStatus("success", total, total,
                        replay ? "重新同步完成（本地联调桩）" : "同步完成（本地联调桩）"));
                log.warn("[本地联调桩] 模拟同步已完成，userCode={}", userCode);
            } catch (Exception e) {
                log.error("[本地联调桩] 模拟同步落终态失败，userCode={}", userCode, e);
            }
        }, fakeDurationSeconds, TimeUnit.SECONDS);
    }

    /**
     * 读取同步状态：与真实实现共用同一 Redis key，前端无感知。
     *
     * @return 同步状态，无记录返回 null
     */
    @Override
    public PersonalSyncStatus getSyncStatus() {
        String json = stringRedisTemplate.opsForValue().get(SYNC_STATUS_KEY_PREFIX + currentUserCode());
        if (StringUtils.isBlank(json)) {
            return null;
        }
        return JSON.parseObject(json, PersonalSyncStatus.class);
    }

    /**
     * 返回内置模拟文档列表，不查询 MySQL。
     * <p>
     * 关键词/渠道筛选语义与真实 list 保持一致，便于一并联调筛选交互；分页在内存中切片。
     */
    @Override
    public PageResponse<DocumentOut> list(Integer pageNum, Integer pageSize, String keyword, String channel) {
        int page = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int size = (pageSize == null || pageSize < 1) ? 10 : pageSize;

        List<DocumentOut> filtered = new ArrayList<>();
        for (DocumentOut doc : mockDocuments()) {
            // 关键词同时匹配文档名称与业务文档ID，与真实查询口径一致
            if (StringUtils.isNotBlank(keyword)
                    && !StringUtils.contains(doc.getName(), keyword)
                    && !StringUtils.contains(doc.getDocId(), keyword)) {
                continue;
            }
            if (StringUtils.isNotBlank(channel) && !channel.equals(doc.getChannel())) {
                continue;
            }
            filtered.add(doc);
        }

        int from = Math.min((page - 1) * size, filtered.size());
        int to = Math.min(from + size, filtered.size());

        PageResponse<DocumentOut> response = new PageResponse<>();
        response.setRecords(new ArrayList<>(filtered.subList(from, to)));
        response.setPageNum(page);
        response.setPageSize(size);
        response.setTotalCount((long) filtered.size());
        response.setTotalPage((int) Math.ceil(filtered.size() * 1.0 / size));
        response.setSuccessCode();
        return response;
    }

    /**
     * 构造模拟文档列表（全部按「企业知识库(zsk)同步」来源返回，模拟同步后的落库结果）。
     */
    private List<DocumentOut> mockDocuments() {
        String userCode = currentUserCode();
        long now = System.currentTimeMillis();
        List<DocumentOut> documents = new ArrayList<>();
        for (int i = 0; i < mockDocCount; i++) {
            String[] template = MOCK_TEMPLATES[i % MOCK_TEMPLATES.length];
            DocumentOut doc = new DocumentOut();
            doc.setId(MOCK_ID_BASE + i);
            doc.setDocId(MOCK_DOC_ID_PREFIX + String.format("%04d", i + 1));
            doc.setChannel("zzccZSK_personal");
            doc.setName(template[0]);
            doc.setFileType(template[1]);
            doc.setFileSize(template[2]);
            doc.setTaskStatus(template[3]);
            doc.setDocType("personal");
            doc.setDocTypeName("个人知识库");
            doc.setSourceName("个人知识库");
            doc.setSourceStatus("normal");
            doc.setSwitchStatus(true);
            doc.setVectorEnabled("enabled");
            doc.setStatus("active");
            doc.setIsAttachment(0);
            doc.setCreator(userCode);
            doc.setModifier(userCode);
            doc.setCreateTime(new Date(now - i * 3600_000L));
            doc.setUpdateTime(new Date(now - i * 1800_000L));
            documents.add(doc);
        }
        return documents;
    }

    /**
     * 写入同步状态到 Redis（TTL 与真实实现一致）。
     */
    private void writeStatus(String userCode, PersonalSyncStatus status) {
        stringRedisTemplate.opsForValue().set(
                SYNC_STATUS_KEY_PREFIX + userCode,
                JSON.toJSONString(status),
                SYNC_STATUS_EXPIRE_SECONDS,
                TimeUnit.SECONDS);
    }

    /**
     * 构建同步状态对象。
     */
    private PersonalSyncStatus buildStatus(String status, int processed, int total, String message) {
        PersonalSyncStatus result = new PersonalSyncStatus();
        result.setStatus(status);
        result.setProcessed(processed);
        result.setTotal(total);
        result.setMessage(message);
        result.setLastSyncTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        return result;
    }

    /**
     * 当前登录用户工号；未登录时退化为固定标识，保证桩可独立运行不报错。
     */
    private String currentUserCode() {
        String userCode = UserInfoContextHolder.getUserInfo();
        return StringUtils.isBlank(userCode) ? "local-stub-user" : userCode;
    }
}
