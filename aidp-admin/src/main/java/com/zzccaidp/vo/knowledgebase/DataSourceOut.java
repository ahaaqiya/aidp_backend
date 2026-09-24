package com.zzccaidp.vo.knowledgebase;

import java.util.Date;

public class DataSourceOut {
    private Long id;
    private String name;
    private String channel;
    private String description;
    private Integer documentCount;
    private Date lastSyncTime;
    private String typeDistribution;
    private Date createTime;
    private Date updateTime;
    private String config;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDocumentCount() { return documentCount; }
    public void setDocumentCount(Integer documentCount) { this.documentCount = documentCount; }

    public Date getLastSyncTime() { return lastSyncTime; }
    public void setLastSyncTime(Date lastSyncTime) { this.lastSyncTime = lastSyncTime; }

    public String getTypeDistribution() { return typeDistribution; }
    public void setTypeDistribution(String typeDistribution) { this.typeDistribution = typeDistribution; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }
}
