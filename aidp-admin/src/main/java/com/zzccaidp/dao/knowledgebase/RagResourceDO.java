package com.zzccaidp.dao.knowledgebase;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 09:16
 * @Version: 1.0
 */
@Data
@Table(name = "zzccaidp_rag_resource")
public class RagResourceDO {
    /**
     * 接入渠道资源ID
     */
    @Id
    @Column(name = "rag_resource_id")
    private String ragResourceId;
    /**
     * 接入渠道，金科系统首字母大写缩写
     */
    @Column(name = "resource_channel")
    private String resourceChannel;
    /**
     * 知识库文档总数，每次更新跑批完需要更新
     */
    @Column(name = "document_count")
    private Integer documentCount;
    /**
     * 最后一次同步时间
     */
    @Column(name = "last_sync_time")
    private LocalDateTime lastSyncTime;
    /**
     * 同步状态0-同步中，1-空闲
     */
    @Column(name = "sync_status")
    private Integer syncStatus;
    /**
     * 知识库状态，0-禁用，1-启用
     */
    @Column(name = "status")
    private Integer status;
    /**
     * 接入渠道资源名称
     */
    @Column(name = "rag_resource_name")
    private String ragResourceName;
    /**
     * 接入渠道资源描述
     */
    @Column(name = "rag_resource_desc")
    private String ragResourceDesc;
    @Column(name = "gmt_create")
    private LocalDateTime gmtCreate;
    @Column(name = "gmt_create_user")
    private String gmtCreateUser;
    @Column(name = "gmt_modified")
    private LocalDateTime gmtModified;
    @Column(name = "gmt_modified_user")
    private String gmtModifiedUser;
}
