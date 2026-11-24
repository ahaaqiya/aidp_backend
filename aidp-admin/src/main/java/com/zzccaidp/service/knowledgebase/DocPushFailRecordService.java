
package com.zzccaidp.service.knowledgebase;

import com.zzccaidp.common.SnowflakeUtil;
import com.zzccaidp.dao.knowledgebase.DocPushFailRecordDO;
import com.zzccaidp.exception.BusinessException;
import com.zzccaidp.in.DocumentAddIn;
import com.zzccaidp.mapper.knowledgebase.DocPushFailRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 文档推送失败记录服务
 *
 * <p>记录两类失败，按 fail_stage 区分：</p>
 * <ul>
 *     <li><b>accept（受理落库失败）</b>：document 表无记录、从表上不可见，
 *     需关联系统（OA/zsk）重推，故落库留存现场；</li>
 *     <li><b>vector（向量化失败，仅自动链路）</b>：document 表 task_status=failed 可见状态，
 *     但失败原因只在日志且滚动即失，故同步落库留存原因（RagFlow 超时/解析报错等），
 *     供事后排查；手动重推失败不记（操作者界面即时可见结果）。</li>
 * </ul>
 * <p>与 doc_change_record（成功变更历史）语义隔离。</p>
 */
@Service
@Slf4j
public class DocPushFailRecordService {

    /** 失败阶段：受理落库失败（校验/S3 转存/变更记录/document 落库环节，各推送链路统一使用） */
    public static final String STAGE_ACCEPT = "accept";

    /** 失败阶段：向量化失败（仅自动链路：开放接口后台单线程池 + 同步推送链路，document 表 task_status=failed 同步回写） */
    public static final String STAGE_VECTOR = "vector";

    /** 失败原因截断上限（与表字段 fail_reason VARCHAR(2048) 对齐） */
    private static final int FAIL_REASON_MAX_LEN = 2000;

    @Autowired
    private DocPushFailRecordMapper docPushFailRecordMapper;

    /**
     * 记录单文档推送失败（带自身保护）
     *
     * <p>落库动作整体 try-catch 包裹：失败记录写入异常仅输出告警日志，
     * 绝不向推送主流程抛出，避免"记失败"反过来导致推送链路二次失败。</p>
     *
     * @param documentAddIn 失败文档的推送入参（可能为 null，仅损失摘要字段）
     * @param failStage     失败阶段，取本类 STAGE_* 常量
     * @param e             失败异常（原因提取：业务异常取 errMsg，其他取 message）
     */
    public void recordFail(DocumentAddIn documentAddIn, String failStage, Exception e) {
        try {
            DocPushFailRecordDO record = new DocPushFailRecordDO();
            record.setId(SnowflakeUtil.nextIdStr());
            if (documentAddIn != null) {
                record.setDocId(documentAddIn.getDocId());
                record.setDocName(documentAddIn.getDocName());
                record.setChannel(documentAddIn.getChannel());
                record.setOperatorType(documentAddIn.getDocOperatorType());
                record.setBackName(documentAddIn.getBackName());
                record.setAwsFilePath(documentAddIn.getAwsFilePath());
            }
            record.setFailStage(failStage);
            record.setFailReason(resolveReason(e));
            record.setFailTime(new Date());
            docPushFailRecordMapper.insertSelective(record);
        } catch (Exception ex) {
            log.warn("文档推送失败记录落库异常, docId={}, failStage={}",
                    documentAddIn == null ? null : documentAddIn.getDocId(), failStage, ex);
        }
    }

    /**
     * 提取失败原因可读文案：业务异常取 errMsg（含具体校验/缺失信息），
     * 其他异常取 message，均拿不到时给固定文案；单条超长时截断。
     */
    private String resolveReason(Exception e) {
        if (e instanceof BusinessException) {
            String errMsg = ((BusinessException) e).getErrMsg();
            if (errMsg != null && !errMsg.isEmpty()) {
                return truncate(errMsg);
            }
        }
        String msg = e == null ? null : e.getMessage();
        return (msg == null || msg.isEmpty()) ? "未知异常" : truncate(msg);
    }

    /** 按表字段长度截断，超长尾部补省略号标识 */
    private String truncate(String text) {
        if (text.length() <= FAIL_REASON_MAX_LEN) {
            return text;
        }
        return text.substring(0, FAIL_REASON_MAX_LEN) + "...";
    }
}
