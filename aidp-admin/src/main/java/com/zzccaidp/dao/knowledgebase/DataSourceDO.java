package com.zzccaidp.dao.knowledgebase;

import java.util.Date;
import javax.persistence.*;

@Table(name = "data_source")
public class DataSourceDO {
    /**
     * 主键ID，自增
     */
    @Id
    private Long id;

    /**
     * 数据源名称，如：OA系统、企业知识库
     */
    private String name;

    /**
     * 文档来源渠道标识（唯一），按照金科系统首字母大写区分
     */
    private String channel;

    /**
     * 数据源描述信息
     */
    private String description;

    /**
     * 该数据源下的文档数量
     */
    @Column(name = "document_count")
    private Integer documentCount;

    /**
     * 最后同步时间
     */
    @Column(name = "last_sync_time")
    private Date lastSyncTime;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 文档类型分布统计，JSON格式存储
     */
    @Column(name = "type_distribution")
    private String typeDistribution;

    /**
     * 数据源配置信息，JSON格式存储
     */
    private String config;

    /**
     * 获取主键ID，自增
     *
     * @return id - 主键ID，自增
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置主键ID，自增
     *
     * @param id 主键ID，自增
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取数据源名称，如：OA系统、企业知识库
     *
     * @return name - 数据源名称，如：OA系统、企业知识库
     */
    public String getName() {
        return name;
    }

    /**
     * 设置数据源名称，如：OA系统、企业知识库
     *
     * @param name 数据源名称，如：OA系统、企业知识库
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    /**
     * 获取文档来源渠道标识（唯一），按照金科系统首字母大写区分
     *
     * @return channel - 文档来源渠道标识（唯一），按照金科系统首字母大写区分
     */
    public String getChannel() {
        return channel;
    }

    /**
     * 设置文档来源渠道标识（唯一），按照金科系统首字母大写区分
     *
     * @param channel 文档来源渠道标识（唯一），按照金科系统首字母大写区分
     */
    public void setChannel(String channel) {
        this.channel = channel == null ? null : channel.trim();
    }

    /**
     * 获取数据源描述信息
     *
     * @return description - 数据源描述信息
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置数据源描述信息
     *
     * @param description 数据源描述信息
     */
    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    /**
     * 获取该数据源下的文档数量
     *
     * @return document_count - 该数据源下的文档数量
     */
    public Integer getDocumentCount() {
        return documentCount;
    }

    /**
     * 设置该数据源下的文档数量
     *
     * @param documentCount 该数据源下的文档数量
     */
    public void setDocumentCount(Integer documentCount) {
        this.documentCount = documentCount;
    }

    /**
     * 获取最后同步时间
     *
     * @return last_sync_time - 最后同步时间
     */
    public Date getLastSyncTime() {
        return lastSyncTime;
    }

    /**
     * 设置最后同步时间
     *
     * @param lastSyncTime 最后同步时间
     */
    public void setLastSyncTime(Date lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    /**
     * 获取创建时间
     *
     * @return create_time - 创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 设置创建时间
     *
     * @param createTime 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取更新时间
     *
     * @return update_time - 更新时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 设置更新时间
     *
     * @param updateTime 更新时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 获取文档类型分布统计，JSON格式存储
     *
     * @return type_distribution - 文档类型分布统计，JSON格式存储
     */
    public String getTypeDistribution() {
        return typeDistribution;
    }

    /**
     * 设置文档类型分布统计，JSON格式存储
     *
     * @param typeDistribution 文档类型分布统计，JSON格式存储
     */
    public void setTypeDistribution(String typeDistribution) {
        this.typeDistribution = typeDistribution == null ? null : typeDistribution.trim();
    }

    /**
     * 获取数据源配置信息，JSON格式存储
     *
     * @return config - 数据源配置信息，JSON格式存储
     */
    public String getConfig() {
        return config;
    }

    /**
     * 设置数据源配置信息，JSON格式存储
     *
     * @param config 数据源配置信息，JSON格式存储
     */
    public void setConfig(String config) {
        this.config = config == null ? null : config.trim();
    }
}