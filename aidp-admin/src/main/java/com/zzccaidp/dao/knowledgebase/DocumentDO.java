package com.zzccaidp.dao.knowledgebase;

import lombok.Data;

import java.util.Date;
import javax.persistence.*;

@Table(name = "document")
@Data
public class DocumentDO {
    /**
     * 主键ID，自增
     */
    @Id
    private Long id;

    /**
     * 文档唯一标识，业务主键
     */
    @Column(name = "doc_id")
    private String docId;

    /**
     * 数据源ID，关联data_source.id
     */
    @Column(name = "data_source_id")
    private Long dataSourceId;

    /**
     * 渠道标识，关联data_source.channel
     */
    private String channel;

    /**
     * 文档名称
     */
    private String name;

    /**
     * 文件类型，如：pdf、docx、txt
     */
    @Column(name = "file_type")
    private String fileType;

    /**
     * 文档类型编码，关联doc_type.code
     */
    @Column(name = "doc_type")
    private String docType;

    /**
     * 文档类型名称
     */
    @Column(name = "doc_type_name")
    private String docTypeName;

    /**
     * 来源名称
     */
    @Column(name = "source_name")
    private String sourceName;

    @Column(name = "source_status")
    private String sourceStatus;

    /**
     * 开关状态：0-关闭，1-开启
     */
    @Column(name = "switch_status")
    private Boolean switchStatus;

    /**
     * 任务状态：pending(待处理)、processing(处理中)、success(成功)、failed(失败)
     */
    @Column(name = "task_status")
    private String taskStatus;

    /**
     * 最后处理时间
     */
    @Column(name = "last_process_time")
    private Date lastProcessTime;

    /**
     * 文件MD5值，用于校验文件完整性
     */
    private String md5;

    /**
     * 文件大小，如：1024KB
     */
    @Column(name = "file_size")
    private String fileSize;

    /**
     * 创建者
     */
    private String creator;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 修改者
     */
    private String modifier;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 对象存储路径（XSKY）
     */
    @Column(name = "xsky_path")
    private String xskyPath;

    /**
     * 存储桶名称
     */
    @Column(name = "bucket_name")
    private String bucketName;

    /**
     * 桶内文件路径
     */
    @Column(name = "bucket_path")
    private String bucketPath;

    /**
     * 向量化启用状态：true(已启用)、false(未启用)
     */
    @Column(name = "vector_enabled")
    private String vectorEnabled;

    /**
     * 向量数据库索引ID
     */
    @Column(name = "vector_id")
    private String vectorId;

    /**
     * 状态：active(正常)、deleted(已删除)
     */
    private String status;

    /**
     * 可见性配置，JSON格式存储
     */
    private String visibility;

    /**
     * 文档元数据，JSON格式存储
     */
    private String metadata;

    /**
     * 所属正文标题（附件时填写，正文为空）
     */
    @Column(name = "parent_doc_name")
    private String parentDocName;

    /**
     * 是否附件：0-正文，1-附件
     */
    @Column(name = "is_attachment")
    private Integer isAttachment;

    /**
     * 文档自身关联的 dataset_id（个人知识库一人一库时使用，优先于 doc_type 默认关联）
     */
    @Column(name = "own_dataset_id")
    private String ownDatasetId;

    /**
     * 搜索关键词（非数据库字段，用于查询）
     */
    @Transient
    private String keyword;

    @Transient
    private String datasetId;
}
