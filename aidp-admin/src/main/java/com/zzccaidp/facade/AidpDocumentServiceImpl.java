package com.zzccaidp.facade;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zzccaidp.AidpDocumentService;
import com.zzccaidp.common.AmazonS3Util;
import com.zzccaidp.common.DocumentRouter;
import com.zzccaidp.common.SnowflakeUtil;
import com.zzccaidp.dao.knowledgebase.DataSourceDO;
import com.zzccaidp.dao.knowledgebase.DocChangeRecordDO;
import com.zzccaidp.dao.knowledgebase.DocTypeDO;
import com.zzccaidp.dao.knowledgebase.DocumentDO;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.exception.BusinessException;
import com.zzccaidp.in.DocumentAddIn;
import com.zzccaidp.mapper.knowledgebase.DocTypeMapper;
import com.zzccaidp.integration.ragflow.RagFlowIntegrationService;
import com.zzccaidp.integration.ragflow.request.UpdateDocumentRequest;
import com.zzccaidp.integration.ragflow.request.UploadDocumentRequest;
import com.zzccaidp.integration.ragflow.response.UploadDocumentResponse;
import com.zzccaidp.mapper.knowledgebase.DataSourceMapper;
import com.zzccaidp.mapper.knowledgebase.DocumentMapper;
import com.zzccaidp.out.DocumentAddOut;
import com.zzccaidp.service.knowledgebase.DocChangeRecordService;
import com.zzccaidp.service.knowledgebase.DocumentService;
import com.zzccaidp.service.knowledgebase.PersonalDatasetService;
import com.zzccaidp.service.ragFlow.RagFlowService;
import com.zzccaidp.service.ragFlow.dto.RagFlowPipelineDTO;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import static com.zzccaidp.enums.ErrCodeEnum.*;

/**
 * @Description: 文档推送更新接口，涉及文档变更历史记录表（doc_change_record）和文档记录表（document）
 * 1、每次根据文档id docId以及操作类型，更新document表的桶id。
 * 2、在变更历史记录表插入一条文档变更记录，最新记录的桶id与document表的桶id保持一致。其余历史记录的id方便用户预览，观察文档变化
 * RAG检索时只需要在文档记录表获取当前文件最新的document内容。
 * @Author: WB233500
 * @Createtime: 15:02
 * @Version: 1.0
 */
@Slf4j
@Service(value = "aidpDocumentService")
public class AidpDocumentServiceImpl implements AidpDocumentService {

    /**
     * 个人知识库同步渠道（与 zsk 侧 PullDocumentToRagFlowImpl.PERSONAL_CHANNEL 保持一致）
     */
    private static final String PERSONAL_CHANNEL = "zzccZSK_personal";

    /**
     * 个人知识库文档类型 code（对应 doc_type 表 code=personal 的记录）
     */
    private static final String PERSONAL_DOC_TYPE = "personal";

    /** 失败明细单条原因最大长度：防止超长异常栈信息冲垮返回体 */
    private static final int FAIL_DETAIL_ITEM_MAX_LEN = 80;

    /** 失败明细总最大长度：整批全失败时也要控制 resultmsg 体量（dubbo 传输 + 前端弹窗展示） */
    private static final int FAIL_DETAIL_TOTAL_MAX_LEN = 500;

    @Autowired
    private RagFlowService ragFlowService;

    @Autowired
    private DocChangeRecordService docChangeRecordService;

    @Autowired
    private AmazonS3Util amazonS3Util;

    @Value("${bades.file.aws-s3.bucketName}")
    private String backName;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private DocTypeMapper docTypeMapper;

    @Autowired
    private PersonalDatasetService personalDatasetService;

    /**
     * 向量化专用单线程池（AsyncTaskConfig#ragVectorExecutor）：
     * HTTP 开放接口「先落库快速返回、后台逐条向量化」的执行器，单线程串行以保护 RagFlow
     */
    @Autowired
    @Qualifier("ragVectorExecutor")
    private Executor ragVectorExecutor;


    @Override
    public DocumentAddOut addDocument(List<DocumentAddIn> documentAddInList) {
        DocumentAddOut documentAddOut = new DocumentAddOut();
        int success = 0;
        int fail = 0;
        StringBuilder stringBuilder = new StringBuilder();
        // 失败明细：按「docId:原因」逐条拼接，随 resultmsg 一起返回，供调用方（zsk）透传前端同步状态展示
        StringBuilder failDetailBuilder = new StringBuilder();
        try {
            for (DocumentAddIn documentAddIn : documentAddInList) {
                // 摘要日志：完整入参由 doc_change_record 落库兜底，避免逐条全量 JSON 打爆日志
                log.info("更新RAG知识库文档内容开始 docId={}, docName={}, channel={}",
                        documentAddIn.getDocId(), documentAddIn.getDocName(), documentAddIn.getChannel());
                try {
                    extracted(documentAddIn);
                    success += 1;
                } catch (Exception e) {
                    // 失败文档不落 doc_change_record，日志是唯一现场：补打全量入参
                    // 捕获所有异常（而非仅 BusinessException），避免单文档异常冒泡导致整批处理中断
                    log.error("文档加入RagFlow向量数据库失败, documentAddIn=[{}]", JSONObject.toJSONString(documentAddIn), e);
                    fail += 1;
                    stringBuilder.append(documentAddIn.getDocId()).append(",");
                    failDetailBuilder.append(documentAddIn.getDocId()).append(":")
                            .append(resolveFailReason(e)).append("；");
                }
            }
            if(!documentAddInList.isEmpty()){
                dataSourceMapper.updateUpdTimeByChannel(documentAddInList.get(0).getChannel(), new Date());
            }
            documentAddOut.setSuccessSum(String.valueOf(success));
            documentAddOut.setFailSum(String.valueOf(fail));
            // 根据失败数设置返回码：全成功=AI0000，有失败=AI0021（调用方按 resultcode 判断，不能误判为成功）
            if (fail == 0) {
                documentAddOut.setResultmsg(SUCCESS.getErrMsg());
                documentAddOut.setResultcode(SUCCESS.getErrCode());
            } else {
                // 失败原因明细附在 resultmsg 尾部（不加 DTO 新字段，zsk 侧旧 jar 天然兼容），总量截断保护
                String failDetails = failDetailBuilder.toString();
                if (failDetails.endsWith("；")) {
                    failDetails = failDetails.substring(0, failDetails.length() - 1);
                }
                documentAddOut.setResultmsg(M0021.getErrMsg() + "，成功" + success + "条，失败" + fail + "条"
                        + buildFailDetailMsg(failDetails));
                documentAddOut.setResultcode(M0021.getErrCode());
            }
            // 去掉失败 docIds 尾部多余逗号
            String failedDocIds = stringBuilder.toString();
            if (failedDocIds.endsWith(",")) {
                failedDocIds = failedDocIds.substring(0, failedDocIds.length() - 1);
            }
            documentAddOut.setDocIds(failedDocIds);
        } catch (Exception e) {
            log.error("文本向量化失败:{},{}", M7001.getErrCode(), M7001.getErrMsg(), e);
            if (e instanceof BusinessException) {
                documentAddOut.setResultcode(((BusinessException) e).getErrCode());
                documentAddOut.setResultmsg(((BusinessException) e).getErrMsg());
            } else {
                documentAddOut.setResultcode(M7001.getErrCode());
                documentAddOut.setResultmsg(M7001.getErrMsg());
            }
        }
        return documentAddOut;
    }

    /**
     * 提取单条文档处理失败的可读原因。
     * 业务异常取 errMsg（含具体校验/缺失信息），其他异常取 getMessage，均拿不到时给固定文案，
     * 避免失败明细里出现空原因；单条超长时截断。
     */
    private String resolveFailReason(Exception e) {
        if (e instanceof BusinessException) {
            String errMsg = ((BusinessException) e).getErrMsg();
            if (errMsg != null && !errMsg.isEmpty()) {
                return truncate(errMsg, FAIL_DETAIL_ITEM_MAX_LEN);
            }
        }
        String msg = e.getMessage();
        return (msg == null || msg.isEmpty()) ? "未知异常" : truncate(msg, FAIL_DETAIL_ITEM_MAX_LEN);
    }

    /** 拼接失败明细文案：拼在 resultmsg 尾部，总量超过上限时截断，防止整批失败撑爆返回体。 */
    private String buildFailDetailMsg(String failDetails) {
        if (failDetails == null || failDetails.isEmpty()) {
            return "";
        }
        return "（失败明细：" + truncate(failDetails, FAIL_DETAIL_TOTAL_MAX_LEN) + "）";
    }

    /**
     * 文档推送更新（异步向量化版，供 HTTP 开放接口使用）。
     * <p>
     * 与同步版 {@link #addDocument(List)} 的区别：add/update 文档仅同步完成
     * 「校验 + S3 转存 + 变更记录 + 落库」即计入成功并快速返回，最耗时的 RagFlow 向量化
     * 由 {@code ragVectorExecutor} 单线程池在后台逐条补做；delete 仍走同步版全流程。
     * <p>
     * 返回语义：successSum/failSum 与失败明细仅反映「落库受理」结果；
     * 后台向量化失败通过日志与管理后台文档列表的 task_status=failed 暴露，
     * 可由源系统再次推送同文档自愈（update 链路先删旧向量再重传）。
     * <p>
     * 性能：落库段（S3 上传 + 多条 SQL）在主线程内按入参顺序逐条串行执行，
     * 以保证同一 docId 的先后语义；聚合结果与同步版完全一致。
     */
    @Override
    public DocumentAddOut addDocumentAsync(List<DocumentAddIn> documentAddInList) {
        DocumentAddOut documentAddOut = new DocumentAddOut();
        int success = 0;
        int fail = 0;
        // 落库受理成功、待后台向量化的文档清单（pending_document 类型不向量化，不进入该清单）
        List<DocumentAddIn> vectorPendingList = new ArrayList<>();
        StringBuilder failedIdsBuilder = new StringBuilder();
        // 失败明细：按「docId:原因」逐条拼接，随 resultmsg 一起返回，供调用方透传展示
        StringBuilder failDetailBuilder = new StringBuilder();
        try {
            for (DocumentAddIn documentAddIn : documentAddInList) {
                // 摘要日志：完整入参由 doc_change_record 落库兜底，避免逐条全量 JSON 打爆日志
                log.info("更新RAG知识库文档内容开始(异步受理) docId={}, docName={}, channel={}",
                        documentAddIn.getDocId(), documentAddIn.getDocName(), documentAddIn.getChannel());
                try {
                    if ("delete".equals(documentAddIn.getDocOperatorType())) {
                        // delete 保持全同步：RagFlow 与 DB 删除顺序耦合（先删 DB 会导致反查不到向量id），且单条调用较快
                        extracted(documentAddIn);
                    } else {
                        DocumentDO documentDO = persistDocument(documentAddIn);
                        // 与同步版 extracted 保持一致：待定文档类型不进入向量化
                        if (!"pending_document".equals(documentDO.getDocType())) {
                            vectorPendingList.add(documentAddIn);
                        }
                    }
                    success += 1;
                } catch (Exception e) {
                    // 受理（落库）失败：捕获所有异常避免单文档异常中断整批，日志补全量入参
                    log.error("文档受理落库失败, documentAddIn=[{}]", JSONObject.toJSONString(documentAddIn), e);
                    fail += 1;
                    failedIdsBuilder.append(documentAddIn.getDocId()).append(",");
                    failDetailBuilder.append(documentAddIn.getDocId()).append(":")
                            .append(resolveFailReason(e)).append("；");
                }
            }
            if (!documentAddInList.isEmpty()) {
                dataSourceMapper.updateUpdTimeByChannel(documentAddInList.get(0).getChannel(), new Date());
            }
            documentAddOut.setSuccessSum(String.valueOf(success));
            documentAddOut.setFailSum(String.valueOf(fail));
            // 根据落库失败数设置返回码：全成功=AI0000，有失败=AI0021（调用方按 resultcode 判断，不能误判为成功）
            if (fail == 0) {
                documentAddOut.setResultmsg(SUCCESS.getErrMsg()
                        + "，已受理" + vectorPendingList.size() + "条向量化任务，后台逐条处理中");
                documentAddOut.setResultcode(SUCCESS.getErrCode());
            } else {
                // 落库失败原因明细附在 resultmsg 尾部，总量截断保护（与同步版一致）
                String failDetails = failDetailBuilder.toString();
                if (failDetails.endsWith("；")) {
                    failDetails = failDetails.substring(0, failDetails.length() - 1);
                }
                documentAddOut.setResultmsg(M0021.getErrMsg() + "，成功" + success + "条，失败" + fail + "条"
                        + buildFailDetailMsg(failDetails));
                documentAddOut.setResultcode(M0021.getErrCode());
            }
            // 去掉失败 docIds 尾部多余逗号
            String failedDocIds = failedIdsBuilder.toString();
            if (failedDocIds.endsWith(",")) {
                failedDocIds = failedDocIds.substring(0, failedDocIds.length() - 1);
            }
            documentAddOut.setDocIds(failedDocIds);
        } catch (Exception e) {
            log.error("文本向量化失败:{},{}", M7001.getErrCode(), M7001.getErrMsg(), e);
            if (e instanceof BusinessException) {
                documentAddOut.setResultcode(((BusinessException) e).getErrCode());
                documentAddOut.setResultmsg(((BusinessException) e).getErrMsg());
            } else {
                documentAddOut.setResultcode(M7001.getErrCode());
                documentAddOut.setResultmsg(M7001.getErrMsg());
            }
        } finally {
            // 提交后台向量化任务：无论返回体组装是否异常，已落库文档必须进入向量化队列，避免漏推
            if (!vectorPendingList.isEmpty()) {
                ragVectorExecutor.execute(() -> vectorizeSequentially(vectorPendingList));
            }
        }
        return documentAddOut;
    }

    /**
     * 异步受理版单条落库：校验 + S3 转存 + 变更记录 + 文档表落库（不含向量化）。
     * 与同步版 extracted 的 add/update 分支保持一致的处理顺序，仅把最耗时的 RagFlow 向量化剥离到后台执行。
     *
     * @return 落库后的文档实体（含 docType，供调用方判断是否需要向量化）
     */
    private DocumentDO persistDocument(DocumentAddIn documentAddIn) {
        //参数校验
        parmentCheck(documentAddIn);
        //拉取文件并转存至 AIDP 桶
        String fileSize = downAndUploadFile(documentAddIn);
        //插入历史记录
        insertDocChangeRecord(documentAddIn);
        //插入/更新 Document 记录（task_status=pending）
        return insertDocument(documentAddIn, fileSize);
    }

    /**
     * 后台逐条向量化（由 ragVectorExecutor 单线程池串行调用）。
     * 单条失败不中断批次：记录全量入参日志并将该文档 task_status 置为 failed，
     * 失败可见渠道为服务日志与管理后台文档列表；源系统再次推送同文档可自愈（update 链路先删旧向量再重传）。
     */
    private void vectorizeSequentially(List<DocumentAddIn> vectorPendingList) {
        int vectorSuccess = 0;
        int vectorFail = 0;
        // 失败文档 docId 清单：随批次完成日志输出，便于直接定位需人工重推的文档
        StringBuilder failedDocIdsBuilder = new StringBuilder();
        for (DocumentAddIn documentAddIn : vectorPendingList) {
            try {
                ragVector(documentAddIn);
                vectorSuccess += 1;
            } catch (Exception e) {
                vectorFail += 1;
                failedDocIdsBuilder.append(documentAddIn.getDocId()).append(",");
                // 失败文档日志补全量入参，便于排查 RagFlow 上传/解析问题
                log.error("后台向量化失败, documentAddIn=[{}]", JSONObject.toJSONString(documentAddIn), e);
                markVectorFailed(documentAddIn);
            }
        }
        // 有失败时在批次摘要中列出失败 docId（单批上限受开放接口 max-doc-count 约束，无超长风险）
        String failedDocIds = failedDocIdsBuilder.toString();
        if (failedDocIds.endsWith(",")) {
            failedDocIds = failedDocIds.substring(0, failedDocIds.length() - 1);
        }
        if (vectorFail > 0) {
            log.info("后台向量化批次完成, 受理={}条, 成功={}条, 失败={}条, 失败docId=[{}]",
                    vectorPendingList.size(), vectorSuccess, vectorFail, failedDocIds);
        } else {
            log.info("后台向量化批次完成, 受理={}条, 成功={}条, 失败={}条",
                    vectorPendingList.size(), vectorSuccess, vectorFail);
        }
    }

    /**
     * 向量化失败后将文档 task_status 回写为 failed，供管理后台文档列表识别失败文档。
     * 状态回写失败仅记录日志，不影响批次内其他文档继续处理。
     */
    private void markVectorFailed(DocumentAddIn documentAddIn) {
        try {
            DocumentDO sourceDoc = findSourceDoc(documentAddIn.getDocId(), documentAddIn.getChannel());
            if (sourceDoc != null) {
                sourceDoc.setTaskStatus("failed");
                documentService.updateByDocId(sourceDoc);
            }
        } catch (Exception e) {
            log.warn("向量化失败状态回写异常, docId={}", documentAddIn.getDocId(), e);
        }
    }

    /** 按最大长度截断字符串，超长时尾部补省略号标识 */
    private String truncate(String text, int maxLen) {
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen) + "...";
    }

    @Transactional
    public void extracted(DocumentAddIn documentAddIn) {

        if ("delete".equals(documentAddIn.getDocOperatorType())) {
            //
            ragFlowService.deleteRagDocument(documentAddIn.getDocId());
            //插入操作历史记录
            insertDocChangeRecord(documentAddIn);
            //删除数据库文档记录以及xsky桶记录
            deleteDocument(documentAddIn);
        } else {
            //参数校验
            parmentCheck(documentAddIn);
            //拉取文件
            String fileSize = downAndUploadFile(documentAddIn);
            //插入历史记录
            insertDocChangeRecord(documentAddIn);
            //上传Document
            DocumentDO documentDO = insertDocument(documentAddIn, fileSize);
            //插入RAG
            if (!"pending_document".equals(documentDO.getDocType())) {
                ragVector(documentAddIn);
            }
        }
    }

    private void deleteDocument(DocumentAddIn documentAddIn) {
        //删除时清掉向量id，更新状态为删除
        DocumentDO sourceDoc = findSourceDoc(documentAddIn.getDocId(), documentAddIn.getChannel());
        if (Objects.isNull(sourceDoc)) {
            // 幂等处理：AIDP 中没有该文档，说明其从未成功推送或已被删除，按「已删除」视为成功。
            // 原实现在此抛 M0017，会让调用方（zsk）把整批判为失败且不标记已同步，导致下一轮重复推送、
            // 永久重试失败——附件（历史未推送过）与主文件均可能命中该场景。
            log.info("删除文档时未找到源记录，按已删除处理，docId={}, channel={}",
                    documentAddIn.getDocId(), documentAddIn.getChannel());
            return;
        }
        documentService.deleteByDocId(sourceDoc.getId());
        //删除xsky桶文件
        amazonS3Util.deleteFile(sourceDoc.getBucketPath(), sourceDoc.getBucketName());
    }

    public boolean extractedLocal(DocumentAddIn documentAddIn) {
        //参数校验
        parmentCheck(documentAddIn);

        try {
            if ("delete".equals(documentAddIn.getDocOperatorType())) {
                ragFlowService.deleteRagDocument(documentAddIn.getDocId());
                insertDocChangeRecord(documentAddIn);
            } else {
                //拉取文件
                String fileSize = downFile(documentAddIn);
                //插入历史记录
                insertDocChangeRecord(documentAddIn);
                //插入RAG
                ragVector(documentAddIn);
            }
            return true;
        } catch (Exception e) {
            // 失败文档不落 doc_change_record，日志是唯一现场：补打全量入参
            log.error("文档向量化失败, documentAddIn=[{}]", JSONObject.toJSONString(documentAddIn), e);
            return false;
        }
    }

    private String downAndUploadFile(DocumentAddIn documentAddIn) {
        log.info("原始文档信息：[{}]", JSONObject.toJSONString(documentAddIn));
        byte[] bytes;
        if ("zzccXBGG".equals(documentAddIn.getChannel())) {
            //OA系统去服务器下载文件，OA没法上传到aws桶
            try {
                bytes = Files.readAllBytes(Paths.get(documentAddIn.getAwsFilePath()));
            } catch (IOException e) {
                log.error("读取OA文件异常,文件DOCID：{}", documentAddIn.getDocId());
                throw new BusinessException(ErrCodeEnum.M0014);
            }
        } else {
            bytes = amazonS3Util.downloadFile(documentAddIn.getAwsFilePath(), documentAddIn.getBackName());
            // downloadFile 内部已捕获异常并返回 null；即便成功也可能下载到 0 字节空文件，两种情况都应拦截，避免上传空文件并走无意义的 RAG 向量化
            if (bytes == null) {
                log.error("下载文件为空,文件DOCID：{}", documentAddIn.getDocId());
                throw new BusinessException(M1036);
            }
        }
        String[] suffix = documentAddIn.getDocName().split("\\.");
        String filePath = amazonS3Util.uploadFile(bytes, suffix[suffix.length - 1], backName);
        documentAddIn.setBackName(backName);
        documentAddIn.setAwsFilePath(filePath);
        log.info("AIDP文档信息：[{}]", JSONObject.toJSONString(documentAddIn));
        return String.valueOf(bytes.length);
    }

    private String downFile(DocumentAddIn documentAddIn) {
        log.info("原始文档信息：[{}]", JSONObject.toJSONString(documentAddIn));
        byte[] bytes = null;
        bytes = amazonS3Util.downloadFile(documentAddIn.getAwsFilePath(), documentAddIn.getBackName());
        return String.valueOf(bytes.length);
    }

    private DocumentDO insertDocument(DocumentAddIn documentAddIn, String fileSize) {
        //根据不同的操作类型处理，新增就插入一条，修改就只更新桶的id，删除，需要更新文档状态为删除
        DocumentDO sourceDoc = findSourceDoc(documentAddIn.getDocId(), documentAddIn.getChannel());
        DataSourceDO dataSourceDO = dataSourceMapper.selectByChannel(documentAddIn.getChannel());
        if (Objects.isNull(sourceDoc)) {
            //没有就新增
            DocumentDO documentDO = getDocumentDO(documentAddIn);
            documentDO.setFileSize(fileSize);
            // data_source 无对应渠道记录时（如个人知识库渠道）不设置数据源，避免 NPE
            if (dataSourceDO != null) {
                documentDO.setDataSourceId(dataSourceDO.getId());
                documentDO.setSourceName(dataSourceDO.getName());
            }
            documentService.addDocument(documentDO);
            return documentDO;
        } else {
            //先删除xskay桶的附件
            amazonS3Util.deleteFile(sourceDoc.getBucketPath(), sourceDoc.getBucketName());
            //有就更新
            if (isPersonalChannel(documentAddIn.getChannel())) {
                // 个人知识库渠道：docType 固定为 personal，并刷新归属人与目标 dataset_id
                // 方案A：doc_type 表不登记 personal，个人库文档不进后台数据资产，故不写 doc_type_name
                //（保持 null，避免经 getDocTypeName 兜底落库「未知」脏值）
                sourceDoc.setDocType(PERSONAL_DOC_TYPE);
                applyPersonalOwnerFields(sourceDoc, documentAddIn);
            } else {
                //sourceDoc.setDocType(DocumentRouter.matchTargetDb(documentAddIn.getDocName()));
                sourceDoc.setDocTypeName(getDocTypeName(sourceDoc.getDocType()));
            }
            sourceDoc.setDocId(documentAddIn.getDocId());
            sourceDoc.setBucketName(documentAddIn.getBackName());
            sourceDoc.setName(documentAddIn.getDocName());
            sourceDoc.setChannel(documentAddIn.getChannel());
            sourceDoc.setMetadata(documentAddIn.getMetadata());
            sourceDoc.setParentDocName(documentAddIn.getParentDocName());
            sourceDoc.setIsAttachment(documentAddIn.getIsAttachment());
            if (dataSourceDO != null) {
                sourceDoc.setSourceName(dataSourceDO.getName());
            }
            sourceDoc.setVectorEnabled("true");
            sourceDoc.setTaskStatus("pending");
            sourceDoc.setStatus("active");
            sourceDoc.setBucketPath(documentAddIn.getAwsFilePath());
            sourceDoc.setUpdateTime(new Date());
            sourceDoc.setFileSize(fileSize);
            String[] fileName = sourceDoc.getName().split("\\.");
            sourceDoc.setFileType(fileName[fileName.length - 1]);
            if (dataSourceDO != null) {
                sourceDoc.setDataSourceId(dataSourceDO.getId());
            }
            documentService.updateByDocId(sourceDoc);
            return sourceDoc;
        }

    }

    @NotNull
    private DocumentDO getDocumentDO(DocumentAddIn documentAddIn) {
        DocumentDO documentDO = new DocumentDO();
        // OICC 渠道（三合一制度库）文档统一归入法律合规库，其他渠道按文档名关键词路由
        if ("oicc".equals(documentAddIn.getChannel())) {
            documentDO.setDocType("legal_compliance");
        } else if (isPersonalChannel(documentAddIn.getChannel())) {
            // 个人知识库渠道：docType 固定为 personal，不做关键词路由
            documentDO.setDocType(PERSONAL_DOC_TYPE);
        } else {
            documentDO.setDocType(DocumentRouter.matchTargetDb(documentAddIn.getDocName()));
        }
        // 方案A：个人知识库渠道不写 doc_type_name（doc_type 表无 personal 记录，避免落库「未知」脏值）
        if (!isPersonalChannel(documentAddIn.getChannel())) {
            documentDO.setDocTypeName(getDocTypeName(documentDO.getDocType()));
        }
        documentDO.setDocId(documentAddIn.getDocId());
        documentDO.setBucketName(documentAddIn.getBackName());
        documentDO.setName(documentAddIn.getDocName());
        documentDO.setChannel(documentAddIn.getChannel());
        documentDO.setMetadata(documentAddIn.getMetadata());
        documentDO.setParentDocName(documentAddIn.getParentDocName());
        documentDO.setIsAttachment(documentAddIn.getIsAttachment());
        documentDO.setVectorEnabled("true");
        documentDO.setTaskStatus("pending");
        String[] fileName = documentDO.getName().split("\\.");
        documentDO.setFileType(fileName[fileName.length - 1]);
        documentDO.setStatus("active");
        documentDO.setBucketPath(documentAddIn.getAwsFilePath());
        documentDO.setCreateTime(new Date());
        documentDO.setUpdateTime(new Date());
        // 个人知识库渠道：回填归属人（列表按 creator 过滤）与目标 dataset_id（向量化入库依赖）
        if (isPersonalChannel(documentAddIn.getChannel())) {
            applyPersonalOwnerFields(documentDO, documentAddIn);
        }
        return documentDO;
    }

    private void ragVector(DocumentAddIn documentAddIn) {
        RagFlowPipelineDTO ragFlowPipelineDTO = new RagFlowPipelineDTO();
        ragFlowPipelineDTO.setDocId(documentAddIn.getDocId());
        ragFlowService.file2RagFlowPipeline(ragFlowPipelineDTO);
    }

    /**
     * 插入历史记录时，由于现在文档还在业务系统的桶中需要如下步骤：
     * 1、去业务系统的桶下载文件并上传到aidp的桶
     * 2、将变更信息插入到aidp的文档变更记录表
     *
     * @param documentAddIn
     */
    private void insertDocChangeRecord(DocumentAddIn documentAddIn) {
        DocChangeRecordDO docChangeRecordDO = BeanUtil.copyProperties(documentAddIn, DocChangeRecordDO.class);
        docChangeRecordDO.setOperatorType(documentAddIn.getDocOperatorType());
        docChangeRecordDO.setId(SnowflakeUtil.nextIdStr());
        docChangeRecordService.addDocChangeRecord(docChangeRecordDO);
    }

    /**
     * 参数校验
     *
     * @param documentAddIn
     */
    private void parmentCheck(DocumentAddIn documentAddIn) {
        if (Objects.isNull(documentAddIn.getDocId())) {
            throw new BusinessException(M0011, "docId");
        }
        if (Objects.isNull(documentAddIn.getBackName())) {
            throw new BusinessException(M0011, "backName");
        }
        if (Objects.isNull(documentAddIn.getDocName())) {
            throw new BusinessException(M0011, "docName");
        }
        if (Objects.isNull(documentAddIn.getDocOperatorType())) {
            throw new BusinessException(M0011, "docOperatorType");
        }
        if (Objects.isNull(documentAddIn.getChannel())) {
            throw new BusinessException(M0011, "channel");
        }
        if (Objects.isNull(documentAddIn.getMetadata())) {
            throw new BusinessException(M0011, "metadata");
        }
        if (Objects.isNull(documentAddIn.getAwsFilePath())) {
            throw new BusinessException(M0011, "awsFilePath");
        }
    }

    private String getDocTypeName(String docType) {
        // 从 doc_type 表读取库名称，支持数据库配置化管理（新增库无需改代码）
        DocTypeDO docTypeDO = docTypeMapper.selectByCode(docType);
        return docTypeDO != null ? docTypeDO.getName() : "未知";
    }

    /**
     * 判断是否为个人知识库同步渠道。
     * 个人库文档不做关键词路由，docType 固定为 personal，并按归属人隔离数据。
     *
     * @param channel 渠道标识
     * @return true-个人知识库渠道
     */
    private boolean isPersonalChannel(String channel) {
        return PERSONAL_CHANNEL.equals(channel);
    }

    /**
     * 按渠道分流查询「业务文档ID + 渠道」对应的存量文档（删除/更新链路复用）。
     * <p>
     * 公共库与个人库的 datasetId 来源不同：
     * - 公共库：datasetId 存于 doc_type.dataset_id，走 findByDocIdAndChannel（连表 doc_type）；
     * - 个人库：doc_type 表不登记 personal，datasetId 存于文档自身 own_dataset_id，
     *   走 selectPersonalByDocIdAndChannel（不连表），避免被 INNER JOIN 过滤而误判「文档不存在」。
     *
     * @param docId   业务文档ID
     * @param channel 来源渠道
     * @return 存量文档，不存在时返回 null
     */
    private DocumentDO findSourceDoc(String docId, String channel) {
        if (isPersonalChannel(channel)) {
            return documentService.selectPersonalByDocIdAndChannel(docId, channel);
        }
        return documentService.findByDocIdAndChannel(docId, channel);
    }

    /**
     * 个人知识库渠道专属字段回填：归属人（creator/modifier）与目标 dataset_id。
     * 个人库列表按 creator=工号 过滤，向量化链路依赖 dataset_id，二者缺一不可。
     *
     * @param documentDO    待落库的文档实体
     * @param documentAddIn zsk 推送入参
     */
    private void applyPersonalOwnerFields(DocumentDO documentDO, DocumentAddIn documentAddIn) {
        String userCode = resolvePersonalUserCode(documentAddIn);
        if (userCode != null) {
            documentDO.setCreator(userCode);
            documentDO.setModifier(userCode);
        }
        documentDO.setOwnDatasetId(resolvePersonalDatasetId(documentAddIn, userCode));
    }

    /**
     * 解析个人知识库文档的归属人工号。
     * zsk 推送的 metadata.permission 为该文档可见用户列表，个人库仅本人可见，取首个即为归属人。
     *
     * @param documentAddIn zsk 推送入参
     * @return 归属人工号，解析失败返回 null
     */
    private String resolvePersonalUserCode(DocumentAddIn documentAddIn) {
        try {
            JSONObject metadata = JSON.parseObject(documentAddIn.getMetadata());
            if (metadata == null) {
                return null;
            }
            JSONArray permission = metadata.getJSONArray("permission");
            if (permission == null || permission.isEmpty()) {
                return null;
            }
            return permission.getString(0);
        } catch (Exception e) {
            log.warn("解析个人知识库文档归属人失败, docId={}, metadata={}",
                    documentAddIn.getDocId(), documentAddIn.getMetadata(), e);
            return null;
        }
    }

    /**
     * 解析个人知识库文档落库的目标 dataset_id。
     * 优先使用 zsk 推送时透传的 dataset 字段（AIDP 发起同步时指定，免去反查）；
     * 未透传时兜底按归属人工号查 personal_dataset 表。
     *
     * @param documentAddIn zsk 推送入参
     * @param userCode      归属人工号
     * @return dataset_id，均未取到时返回 null
     */
    private String resolvePersonalDatasetId(DocumentAddIn documentAddIn, String userCode) {
        String datasetId = documentAddIn.getDataset();
        if (datasetId == null || datasetId.trim().isEmpty()) {
            datasetId = personalDatasetService.getPersonalDatasetId(userCode);
        }
        if (datasetId == null || datasetId.trim().isEmpty()) {
            log.warn("个人知识库文档未解析到 datasetId, docId={}, userCode={}", documentAddIn.getDocId(), userCode);
            return null;
        }
        return datasetId;
    }
}
