package com.zzccaidp.vo.knowledgebase;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 10:51
 * @Version: 1.0
 */
@Data
public class RagResourceIn {
    /**
     * 接入渠道资源ID
     */
    private String ragResourceId;
    /**
     * 接入渠道，金科系统首字母大写缩写
     */
    private String resourceChannel;
    /**
     * 知识库文档总数，每次更新跑批完需要更新
     */
    private Integer documentCount = 0;
    /**
     * 最后一次同步时间
     */
    private LocalDateTime lastSyncTime;
    /**
     * 同步状态0-同步中，1-空闲
     */
    private Integer syncStatus;
    /**
     * 知识库状态，0-禁用，1-启用
     */
    private Integer status;
    /**
     * 接入渠道资源名称
     */
    private String ragResourceName;
    /**
     * 接入渠道资源描述
     */
    private String ragResourceDesc;
    private LocalDateTime gmtCreate;
    private String gmtCreateUser;
    private LocalDateTime gmtModified;
    private String gmtModifiedUser;
}
