package com.zzccaidp.service.knowledgebase;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.athena.api.PullDocumentToRagFlow;
import com.athena.api.out.PersonalDocSyncRes;
import com.zzccaidp.async.TransactionAsyncExecutor;
import com.zzccaidp.common.AmazonS3Util;
import com.zzccaidp.context.UserInfoContextHolder;
import com.zzccaidp.dao.knowledgebase.DocumentDO;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.exception.BusinessException;
import com.zzccaidp.mapper.knowledgebase.DocumentMapper;
import com.zzccaidp.service.ragFlow.RagFlowService;
import com.zzccaidp.service.ragFlow.dto.RagFlowPipelineDTO;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.knowledgebase.DocumentOut;
import com.zzccaidp.vo.knowledgebase.PersonalSyncStatus;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 个人知识库文档服务：提供「个人文档列表查询」、「个人文档上传」与「从 zsk 同步个人文档」。
 * <p>
 * 与公共知识库的差异：
 * 1. 数据隔离采用「一人一个dataset」，通过 personal_dataset 表维护「工号 → dataset_id」映射；
 * 2. 文档落库时直接写入 document.own_dataset_id（ownDatasetId），向量化时优先使用该值；
 * 3. metadata.permission 写入当前工号，用于检索侧的权限过滤。
 * <p>
 * 同步说明：AIDP 不再主动通过 HTTP 拉取 zsk 文档，改为调用 zsk 提供的 Dubbo 接口
 * {@link PullDocumentToRagFlow#pullPersonalDocument(String, String)}，由 zsk 侧拉取后反向回调
 * {@code com.zzccaidp.AidpDocumentService#addDocument} 完成落库。
 */
@Slf4j
@Service
public class PersonalDocumentService {

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private AmazonS3Util amazonS3Util;

    @Autowired
    private RagFlowService ragFlowService;

    /** 个人 dataset 解析（一人一库），与推送落库侧共用 */
    @Autowired
    private PersonalDatasetService personalDatasetService;

    /** zsk 文档推送 Dubbo 接口（消费方） */
    @Autowired
    private PullDocumentToRagFlow pullDocumentToRagFlow;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private TransactionAsyncExecutor transactionAsyncExecutor;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${bades.file.aws-s3.bucketName}")
    private String bucketName;

    /** 个人文档类型编码（doc_type 表需预置该记录） */
    private static final String PERSONAL_DOC_TYPE = "personal";

    /** 个人文档类型名称 */
    private static final String PERSONAL_DOC_TYPE_NAME = "个人知识库";

    /** 个人文档渠道标识（用户直接上传） */
    private static final String PERSONAL_CHANNEL = "PERSONAL";

    /** 个人知识库同步状态 Redis key 前缀（key = 前缀 + 工号） */
    private static final String PERSONAL_SYNC_STATUS_KEY_PREFIX = "PERSONAL_SYNC_STATUS_";

    /** 同步状态 Redis 过期时间（秒），临时状态无需持久化 */
    private static final long SYNC_STATUS_EXPIRE_SECONDS = 24 * 3600L;

    /** running 状态超时阈值（分钟）：超过该时长仍未落终态的同步任务视为僵尸锁，允许重新发起同步 */
    private static final long SYNC_RUNNING_TIMEOUT_MINUTES = 30L;

    /** 个人库同步时间参数统一格式（与 zsk 侧 TIME_FORMATTER 保持一致） */
    private static final DateTimeFormatter SYNC_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 分页查询当前用户的个人知识库文档列表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param keyword  名称/文档ID 关键词（可选）
     * @param channel  来源渠道（可选）：PERSONAL-本地上传 / zzccZSK_personal-企业知识库(zsk)同步；为空表示不限
     * @return 分页结果
     */
    public PageResponse<DocumentOut> list(Integer pageNum, Integer pageSize, String keyword, String channel) {
        String userCode = UserInfoContextHolder.getUserInfo();
        DocumentDO query = new DocumentDO();
        query.setDocType(PERSONAL_DOC_TYPE);
        query.setCreator(userCode);
        query.setKeyword(keyword);
        query.setChannel(channel);

        PageHelper.startPage(pageNum, pageSize);
        // 走个人知识库专用查询：直接取 document.own_dataset_id 作为 datasetId，不依赖 doc_type 关联
        List<DocumentDO> documentDOList = documentMapper.selectPersonalByCondition(query);
        PageInfo<DocumentDO> pageInfo = new PageInfo<>(documentDOList);

        // 回刷当前页「处理中」文档的向量化进度：个人库 datasetId 存于 document.own_dataset_id，
        // 不能复用后台按 doc_type 反查 datasetId 的链路，这里直接以文档自身的 vectorId + ownDatasetId 查询
        refreshProcessingStatus(pageInfo.getList());

        PageResponse<DocumentOut> page = new PageResponse<>();
        page.setRecords(BeanUtil.copyToList(pageInfo.getList(), DocumentOut.class));
        page.setPageNum(pageInfo.getPageNum());
        page.setPageSize(pageInfo.getPageSize());
        page.setTotalPage(pageInfo.getPages());
        page.setTotalCount(pageInfo.getTotal());
        page.setSuccessCode();
        return page;
    }

    /**
     * 回刷当前页「处理中」文档的向量化进度。
     * <p>
     * 个人知识库的 datasetId 直接存放在 document.own_dataset_id（ownDatasetId），与后台文档按
     * doc_type 反查 datasetId 的链路不同，因此这里直接使用文档自身的 vectorId + ownDatasetId
     * 查询 ragflow 解析状态，将结果映射为 task_status 并回写，保证前端列表状态能自动流转到
     * success/failed，不会一直停留在「处理中」。
     * <p>
     * 说明：单个文档查询失败仅记录日志、保留原状态，不影响整个列表返回。
     *
     * @param documents 当前页文档列表（命中记录的 taskStatus 会被就地更新）
     */
    private void refreshProcessingStatus(List<DocumentDO> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        for (DocumentDO doc : documents) {
            // 除 processing 外，pending 也必须参与回刷：历史版本会把 ragflow 未开始/排队中的文档
            // 回写成 pending，而旧逻辑只轮询 processing，导致这些记录成为永不再流转的死状态
            if ((!"processing".equals(doc.getTaskStatus()) && !"pending".equals(doc.getTaskStatus()))
                    || StringUtils.isBlank(doc.getVectorId())
                    || StringUtils.isBlank(doc.getOwnDatasetId())) {
                continue;
            }
            try {
                String run = ragFlowService.getDocumentRunStatus(doc.getVectorId(), doc.getOwnDatasetId());
                String taskStatus = mapRunStatus2TaskStatus(run);
                // taskStatus 为 null 表示 ragflow 尚未给出确定结果（未开始/排队中/查询不到），
                // 保持现有状态；不可回写 pending，否则会再次形成死状态
                if (taskStatus != null && !taskStatus.equals(doc.getTaskStatus())) {
                    documentMapper.updateTaskStatus(doc.getId(), taskStatus);
                    doc.setTaskStatus(taskStatus);
                }
            } catch (Exception e) {
                log.warn("刷新个人知识库文档向量化状态失败，docId={}", doc.getDocId(), e);
            }
        }
    }

    /**
     * ragflow run 状态映射为本地 task_status。
     * <p>
     * ragflow 官方 TaskStatus：0=UNSTART、1=RUNNING、2=CANCEL、3=DONE、4=FAIL、5=SCHEDULE。
     * 映射规则：1/RUNNING → processing；2/CANCEL、3/DONE → success；4/FAIL → failed；
     * 0/UNSTART、5/SCHEDULE 及查询为空（null）返回 null，表示「暂无确定结果」，由调用方保持原状态，
     * 不得回写 pending。
     *
     * @param run ragflow 返回的 run 状态，可能为 null
     * @return 本地 task_status；返回 null 表示状态未确定、无需更新
     */
    private String mapRunStatus2TaskStatus(String run) {
        if (run == null) {
            return "pending";
        }
        switch (run) {
            case "1":
            case "RUNNING":
                return "processing";
            case "2":
            case "DONE":
                return "success";
            case "3":
            case "FAIL":
                return "failed";
            case "0":
            case "UNSTART":
            default:
                return "pending";
        }
    }

    /**
     * 上传个人知识库文档（上传即触发向量化）。
     * <p>
     * 单个文档处理流程：懒创建/复用个人 dataset → 上传 XSKY → 落 document 表 → 触发 ragflow 向量化。
     * 任一文档处理异常会抛出并回滚整批（与上传接口 @Transactional 保持一致）。
     *
     * @param files 待上传文件数组
     * @return 上传成功的文档列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<DocumentOut> upload(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BusinessException(ErrCodeEnum.M1001);
        }
        String userCode = UserInfoContextHolder.getUserInfo();
        // 懒创建/复用个人 dataset（同一用户一次请求只创建一次）
        String datasetId = personalDatasetService.getOrCreatePersonalDataset(userCode);

        List<DocumentOut> result = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            result.add(uploadOne(files[i], userCode, datasetId, i));
        }
        return result;
    }

    /**
     * 上传单个文档并触发向量化
     */
    private DocumentOut uploadOne(MultipartFile file, String userCode, String datasetId, int index) {
        String originalFilename = FilenameUtils.getName(file.getOriginalFilename());
        String extension = FilenameUtils.getExtension(originalFilename);
        byte[] fileContent;
        try {
            fileContent = file.getBytes();
        } catch (IOException e) {
            log.error("读取上传文件失败：{}", originalFilename, e);
            throw new BusinessException(ErrCodeEnum.M1035);
        }

        // 上传文件到 XSKY，得到桶内文件路径
        String filePath = amazonS3Util.uploadFile(fileContent, extension, bucketName);

        DocumentDO doc = new DocumentDO();
        doc.setDocId("DOC-" + System.currentTimeMillis() + "-" + index);
        doc.setChannel(PERSONAL_CHANNEL);
        doc.setName(originalFilename);
        doc.setFileType(getFileType(originalFilename));
        doc.setDocType(PERSONAL_DOC_TYPE);
        doc.setDocTypeName(PERSONAL_DOC_TYPE_NAME);
        doc.setSourceName(PERSONAL_DOC_TYPE_NAME);
        doc.setSourceStatus("normal");
        doc.setTaskStatus("pending");
        doc.setSwitchStatus(true);
        doc.setVectorEnabled("enabled");
        doc.setFileSize(String.valueOf(fileContent.length));
        doc.setCreator(userCode);
        doc.setModifier(userCode);
        doc.setBucketName(bucketName);
        doc.setBucketPath(filePath);
        doc.setCreateTime(new Date());
        doc.setUpdateTime(new Date());
        doc.setStatus("active");
        // 关键：个人文档直接指定 own_dataset_id（一人一个dataset），而非依赖 doc_type 关联
        doc.setOwnDatasetId(datasetId);

        // metadata.permission 写入当前工号，供检索侧权限过滤
        JSONObject metadata = new JSONObject();
        metadata.put("permission", userCode);
        doc.setMetadata(metadata.toJSONString());

        documentMapper.insert(doc);

        // 上传即触发向量化（同步，完成后 task_status 置为 processing，由 ragflow 后台继续解析）
        RagFlowPipelineDTO dto = new RagFlowPipelineDTO();
        dto.setDocId(doc.getDocId());
        dto.setVectorEnable(true);
        ragFlowService.file2RagFlowPipeline(dto);

        // 重新查询，返回向量化后的最新状态（vectorId / taskStatus）
        DocumentDO latest = documentMapper.selectPersonalByDocId(doc.getDocId());
        return BeanUtil.copyProperties(latest, DocumentOut.class);
    }

    /**
     * 根据文件名后缀判断文件类型
     */
    private String getFileType(String fileName) {
        if (fileName == null) {
            return "other";
        }
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (ext) {
            case "pdf":
                return "pdf";
            case "doc":
            case "docx":
                return "docx";
            case "xls":
            case "xlsx":
                return "xlsx";
            case "txt":
                return "txt";
            case "md":
                return "markdown";
            default:
                return "other";
        }
    }

    /**
     * 删除个人知识库文档（物理删除）。
     * <p>
     * 单个文档处理流程：查询记录 → 越权校验（仅本人 personal 类型）→ 删除 ragflow 向量 →
     * 删除 XSKY 物理文件 → 移除 document 记录。任一步失败抛出异常回滚整批，
     * 保证 ragflow / XSKY / DB 三处状态一致，不残留脏数据。
     *
     * @param docIds 业务文档ID（docId）列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> docIds) {
        if (docIds == null || docIds.isEmpty()) {
            throw new BusinessException(ErrCodeEnum.M1001);
        }
        String userCode = UserInfoContextHolder.getUserInfo();
        for (String docId : docIds) {
            if (StringUtils.isBlank(docId)) {
                continue;
            }
            DocumentDO doc = documentMapper.selectPersonalByDocId(docId);
            if (doc == null) {
                // 记录不存在说明已被删除，跳过即可，保证批量删除的幂等性
                continue;
            }
            // 越权保护：仅允许删除本人上传的个人知识库文档，防止误删他人或公共库文档
            if (!PERSONAL_DOC_TYPE.equals(doc.getDocType()) || !userCode.equals(doc.getCreator())) {
                log.warn("拒绝删除非本人个人知识库文档，docId={}, operator={}, owner={}", docId, userCode, doc.getCreator());
                throw new BusinessException(ErrCodeEnum.M0005, "无权删除该文档");
            }
            // 先清理 ragflow 向量（清理失败直接中断，避免留下「检索仍可命中」的孤儿文档）
            if (StringUtils.isNotBlank(doc.getVectorId())) {
                if (!ragFlowService.deleteRagDocument(doc.getDocId())) {
                    throw new BusinessException(ErrCodeEnum.M7004);
                }
            }
            // 清理 XSKY 物理文件
            if (StringUtils.isNotBlank(doc.getBucketPath())) {
                amazonS3Util.deleteFile(doc.getBucketPath(), doc.getBucketName());
            }
            // 最后物理删除 document 记录
            documentMapper.deleteById(doc.getId());
        }
    }

    /**
     * 触发个人知识库文档向量化（支持手动重跑）。
     * <p>
     * 与上传链路复用同一个 ragflow 处理管道 {@link RagFlowService#file2RagFlowPipeline}：
     * 管道内部会先删除已存在的旧向量再重新上传与分片，因此对已向量化文档重复点击不会产生重复向量，
     * 对失败文档重跑也可直接覆盖。单个文档处理完成后 task_status 会被置为 processing，
     * 由 ragflow 后台继续解析，前端通过列表的「状态」列查看进度。
     *
     * @param docIds 业务文档ID（docId）列表
     * @return 向量化后的文档列表（含最新 taskStatus / vectorId）
     */
    @Transactional(rollbackFor = Exception.class)
    public List<DocumentOut> process(List<String> docIds) {
        if (docIds == null || docIds.isEmpty()) {
            throw new BusinessException(ErrCodeEnum.M1001);
        }
        String userCode = UserInfoContextHolder.getUserInfo();
        List<DocumentOut> result = new ArrayList<>();
        for (String docId : docIds) {
            if (StringUtils.isBlank(docId)) {
                continue;
            }
            DocumentDO doc = documentMapper.selectPersonalByDocId(docId);
            if (doc == null) {
                // 记录不存在说明已被删除，无法向量化
                throw new BusinessException(ErrCodeEnum.M7102);
            }
            // 越权保护：仅允许操作本人上传的个人知识库文档
            if (!PERSONAL_DOC_TYPE.equals(doc.getDocType()) || !userCode.equals(doc.getCreator())) {
                log.warn("拒绝向量化非本人个人知识库文档，docId={}, operator={}, owner={}", docId, userCode, doc.getCreator());
                throw new BusinessException(ErrCodeEnum.M0005, "无权操作该文档");
            }
            RagFlowPipelineDTO dto = new RagFlowPipelineDTO();
            dto.setDocId(doc.getDocId());
            dto.setVectorEnable(true);
            ragFlowService.file2RagFlowPipeline(dto);

            // 重新查询，返回向量化后的最新状态（vectorId / taskStatus）
            DocumentDO latest = documentMapper.selectPersonalByDocId(doc.getDocId());
            result.add(BeanUtil.copyProperties(latest, DocumentOut.class));
        }
        return result;
    }

    /**
     * 提交个人知识库同步任务（异步）。
     * <p>
     * 复用 {@link #getOrCreatePersonalDataset(String)} 的懒创建逻辑：初次同步自动创建 dataset，后续复用，
     * 无需在业务层单独区分「初次/非初次」。同步过程「新增 + 覆盖更新 + 反向删除」在异步线程中执行，
     * 前端通过 {@link #getSyncStatus()} 轮询进度。
     * <p>
     * running 状态同时充当「幂等锁」：先写锁再投递异步任务，因此投递环节必须有失败补偿，
     * 并对超过 {@link #SYNC_RUNNING_TIMEOUT_MINUTES} 分钟的僵尸 running 状态做自愈，避免用户被永久拦截。
     */
    public void sync(String startTime) {
        String userCode = UserInfoContextHolder.getUserInfo();
        // 重放模式入参前置校验：格式错误直接拒绝提交，避免任务投递后才在异步线程失败
        boolean replay = StringUtils.isNotBlank(startTime);
        if (replay) {
            try {
                LocalDateTime.parse(startTime.trim(), SYNC_TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                throw new BusinessException(ErrCodeEnum.M0005, "startTime 格式错误，应为 yyyy-MM-dd HH:mm:ss");
            }
        }
        // 幂等保护：已有任务在执行则拒绝（读-写之间存在极小竞态窗口，先搭框架阶段接受）
        PersonalSyncStatus current = readSyncStatus(userCode);
        if (current != null && "running".equals(current.getStatus())) {
            // 超时自愈：若任务长时间未落终态（如异步投递失败、进程重启导致任务丢失），running 锁会残留至 24h TTL，
            // 期间用户每次点击都会被 M7108 拦截；此处判定为僵尸锁后放行，允许重新发起同步
            if (!isRunningTimeout(current)) {
                throw new BusinessException(ErrCodeEnum.M7108);
            }
            log.warn("同步 running 状态已超时（起始时间={}），判定为僵尸任务并允许重新发起，userCode={}",
                    current.getLastSyncTime(), userCode);
        }
        // 先写 running（兼作幂等锁）再投递：若投递抛异常必须补偿释放锁，否则锁残留导致后续点击一直被拦截
        try {
            // 注：running 阶段把当前时间写入 lastSyncTime，作为后续超时自愈判断的起始时间
            writeSyncStatus(userCode, buildStatus("running", 0, 0,
                    replay ? "重新同步任务已提交（自 " + startTime.trim() + " 起）" : "同步任务已提交", nowStr()));
            transactionAsyncExecutor.runAfterCommitAsync(() -> doSync(userCode, replay ? startTime.trim() : null));
        } catch (RuntimeException e) {
            log.error("个人知识库同步任务投递失败，回写失败状态释放锁，userCode={}", userCode, e);
            try {
                writeSyncStatus(userCode, buildStatus("failed", 0, 0, "同步任务提交失败：" + e.getMessage(), nowStr()));
            } catch (RuntimeException ex) {
                // 失败状态回写也失败时，直接删除状态 key，确保锁被释放
                log.error("个人知识库同步失败状态回写失败，删除状态 key 释放锁，userCode={}", userCode, ex);
                stringRedisTemplate.delete(PERSONAL_SYNC_STATUS_KEY_PREFIX + userCode);
            }
            throw e;
        }
    }

    /**
     * 查询当前用户的个人知识库同步状态（存于 Redis 临时状态）。
     *
     * @return 同步状态，无记录返回 null
     */
    public PersonalSyncStatus getSyncStatus() {
        String userCode = UserInfoContextHolder.getUserInfo();
        return readSyncStatus(userCode);
    }

    /**
     * 同步任务核心逻辑（异步线程执行）。
     * <p>
     * 流程：懒创建/复用当前用户的个人 dataset → 调用 zsk 的 Dubbo 接口，
     * 由 zsk 侧完成「拉取个人文档 → 反向回调 AIDP addDocument(落库 + 向量化)」。
     * <ul>
     *     <li>startTime 为空：调 {@code pullPersonalDocument}，zsk 按内部水位增量同步；</li>
     *     <li>startTime 非空：调 {@code pullPersonalDocumentFrom}，zsk 从指定起点重放全部变更
     *         （不推进水位），用于恢复 AIDP 侧缺失的文档。</li>
     * </ul>
     * <p>
     * 说明：zsk 侧内部已按用户加锁并维护增量水位，AIDP 只需把 userCode 与目标 datasetId 传过去，
     * 无需再关心「文档该落到哪个库」，也无需反向删除（删除由 zsk 以 delete 记录推送）。
     * <p>
     * 方向1改造：接口已由 void 改为返回 {@link PersonalDocSyncRes}。zsk 侧「工号为空 / 用户不存在 /
     * 未抢到并发锁 / 内部异常」等失败不再抛异常，而是随返回值返回，因此必须按返回值判定成败，
     * 否则会写回「假成功」状态（旧实现固定写 1/1 计数）。
     *
     * @param userCode  用户工号
     * @param startTime 重新同步起点时间（yyyy-MM-dd HH:mm:ss）；null 表示普通增量同步
     */
    private void doSync(String userCode, String startTime) {
        try {
            // 懒创建/复用个人 dataset（初次自动建库），并把 datasetId 透传给 zsk，落库时原样写回 document.own_dataset_id
            String datasetId = personalDatasetService.getOrCreatePersonalDataset(userCode);

            // 调用 zsk Dubbo 接口触发推送（同步等待 zsk 完成拉取与回调落库），按返回结果写回真实状态与计数
            PersonalDocSyncRes result = (startTime == null)
                    ? pullDocumentToRagFlow.pullPersonalDocument(userCode, datasetId)
                    : pullDocumentToRagFlow.pullPersonalDocumentFrom(userCode, datasetId, startTime);

            // 防御：理论上 zsk 侧保证返回结果对象，若为空按失败处理，绝不落假成功
            if (result == null) {
                writeSyncStatus(userCode, buildStatus("failed", 0, 0, "同步失败：zsk 未返回同步结果", nowStr()));
                return;
            }
            if (result.isSuccess()) {
                writeSyncStatus(userCode, buildStatus("success", result.getSuccessCount(), result.getTotal(),
                        StringUtils.isBlank(result.getMessage()) ? "同步完成" : result.getMessage(), nowStr()));
            } else {
                // 透传 zsk 返回的具体失败原因，计数按实际成功数写入
                writeSyncStatus(userCode, buildStatus("failed", result.getSuccessCount(), result.getTotal(),
                        StringUtils.isBlank(result.getMessage()) ? "同步失败" : result.getMessage(), nowStr()));
            }
        } catch (Exception e) {
            log.error("个人知识库{}同步失败，userCode={}, 重放起点={}",
                    startTime == null ? "" : "重新", userCode, startTime, e);
            writeSyncStatus(userCode, buildStatus("failed", 0, 0,
                    (startTime == null ? "同步失败：" : "重新同步失败：") + e.getMessage(), nowStr()));
        }
    }

    /**
     * 查询当前用户个人知识库的上次增量同步水位时间（数据源：zsk 侧 personal_doc_sync_record 表）。
     * <p>
     * 供前端展示「上次同步时间」，并作为「重新同步」起点的默认值；普通同步将从该时间点起增量执行。
     * Dubbo 调用异常直接向上抛出，由全局异常处理透传原始错误。
     *
     * @return 上次同步水位时间（yyyy-MM-dd HH:mm:ss）；当前用户从未同步过返回 null
     */
    public String getLastSyncTime() {
        String userCode = UserInfoContextHolder.getUserInfo();
        return pullDocumentToRagFlow.getLastPersonalSyncTime(userCode);
    }

    /**
     * 写入同步状态到 Redis（临时状态，24h 过期）。
     */
    private void writeSyncStatus(String userCode, PersonalSyncStatus status) {
        stringRedisTemplate.opsForValue().set(
                PERSONAL_SYNC_STATUS_KEY_PREFIX + userCode,
                JSON.toJSONString(status),
                SYNC_STATUS_EXPIRE_SECONDS,
                TimeUnit.SECONDS);
    }

    /**
     * 从 Redis 读取同步状态。
     */
    private PersonalSyncStatus readSyncStatus(String userCode) {
        String json = stringRedisTemplate.opsForValue().get(PERSONAL_SYNC_STATUS_KEY_PREFIX + userCode);
        if (StringUtils.isBlank(json)) {
            return null;
        }
        return JSON.parseObject(json, PersonalSyncStatus.class);
    }

    /**
     * 判断 running 状态是否已超时（僵尸任务）。
     * <p>
     * 复用 lastSyncTime 字段承载「running 起始时间」：任务进入 running 时写入当前时间，落终态时被覆盖为完成/失败时间，
     * 因此该字段在两个阶段的语义是自洽的，无需额外新增字段。
     * 解析失败（字段为空或格式异常）时按「未超时」处理，保守地拒绝重复提交。
     *
     * @param status 当前同步状态（调用方已保证 status == running）
     * @return true-已超时可重新发起；false-仍在执行中
     */
    private boolean isRunningTimeout(PersonalSyncStatus status) {
        String startTime = status.getLastSyncTime();
        if (StringUtils.isBlank(startTime)) {
            return false;
        }
        try {
            long startMillis = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startTime).getTime();
            long elapsedMinutes = (System.currentTimeMillis() - startMillis) / (60 * 1000);
            return elapsedMinutes >= SYNC_RUNNING_TIMEOUT_MINUTES;
        } catch (Exception e) {
            log.warn("解析同步状态起始时间失败，按未超时处理，lastSyncTime={}", startTime, e);
            return false;
        }
    }

    /**
     * 构建同步状态对象。
     */
    private PersonalSyncStatus buildStatus(String status, int processed, int total, String message, String lastSyncTime) {
        PersonalSyncStatus s = new PersonalSyncStatus();
        s.setStatus(status);
        s.setProcessed(processed);
        s.setTotal(total);
        s.setMessage(message);
        s.setLastSyncTime(lastSyncTime);
        return s;
    }

    /**
     * 当前时间字符串（yyyy-MM-dd HH:mm:ss）。
     */
    private String nowStr() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
