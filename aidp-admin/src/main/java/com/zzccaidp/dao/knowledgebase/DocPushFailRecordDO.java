package com.zzccaidp.dao.knowledgebase;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 文档推送失败记录（doc_push_fail_record 表）
 *
 * <p>仅记录「落库失败」（accept 阶段）：此时 document 表无记录、从表上不可见，
 * 且需关联系统（OA/zsk）重推，故独立落表留存现场；
 * 落库成功后仅向量化失败的不记录（document 表 task_status=failed 可见）。
 * 与 doc_change_record（成功变更历史）语义隔离。</p>
 */
@Table(name = "doc_push_fail_record")
@Data
public class DocPushFailRecordDO {

    /** 雪花ID */
    @Id
    private String id;

    /** 业务文档ID */
    @Column(name = "doc_id")
    private String docId;

    /** 文档名称 */
    @Column(name = "doc_name")
    private String docName;

    /** 来源渠道 */
    @Column(name = "channel")
    private String channel;

    /** 操作类型：add/update/delete */
    @Column(name = "operator_type")
    private String operatorType;

    /** 源文件桶名 */
    @Column(name = "back_name")
    private String backName;

    /** 源桶内文件路径 */
    @Column(name = "aws_file_path")
    private String awsFilePath;

    /** 失败阶段：accept-受理落库失败 */
    @Column(name = "fail_stage")
    private String failStage;

    /** 失败原因（异常信息，超长截断） */
    @Column(name = "fail_reason")
    private String failReason;

    /** 失败时间 */
    @Column(name = "fail_time")
    private Date failTime;
}
