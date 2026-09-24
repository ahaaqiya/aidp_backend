package com.zzccaidp.dao.knowledgebase;

import java.util.Date;
import javax.persistence.*;

@Table(name = "doc_type")
public class DocTypeDO {
    /**
     * 主键ID，使用UUID作为主键，确保分布式环境下的唯一性
     */
    @Id
    private String id;

    /**
     * 类型编码，业务层面的唯一标识，用于代码中关联和判断，如：technical、product、meeting、report等
     */
    private String code;

    /**
     * 类型名称，用于前端展示，如：技术文档、产品文档、会议纪要、分析报告
     */
    private String name;

    /**
     * 类型描述，说明该文档类型的用途和管理规范
     */
    private String description;

    /**
     * 文档数量，该类型下的文档总数，用于统计和展示
     */
    private Integer count;

    /**
     * 分片方式，标识文档内容的切分策略，取值：auto（智能分片，系统自动根据内容结构分析后分片）、fixed（固定长度分片，按指定字符数均匀切分）、paragraph（段落分片，按自然段落边界切分）、recursive（递归分片，按层级结构递归切分）
     */
    @Column(name = "chunk_method")
    private String chunkMethod;

    /**
     * 来源数量，使用该文档类型的数据源数量
     */
    @Column(name = "source_count")
    private Integer sourceCount;

    /**
     * 更新时间，文档类型最近一次修改的时间
     */
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 类型状态标识，取值：active（正常/激活状态，类型可用）、deleted（已删除状态，不再使用但保留历史数据）
     */
    private String status;

    /**
     * 来源列表，JSON数组格式，记录使用该类型的数据源信息，如：[{"id":1,"name":"数据源A"},{"id":2,"name":"数据源B"}]
     */
    private String sources;

    /**
     * Ragflow数据集ID
     */
    @Column(name = "dataset_id")
    private String datasetId;

    /**
     * 是否参与知识库问答检索，取值：1-参与（该资源库 dataset 进入对话页知识库检索范围）、0-不参与。
     * 供 Python 检索侧按开关过滤公共库 dataset，与文档数据本身的存在状态无关。
     */
    @Column(name = "retrieval_enabled")
    private Integer retrievalEnabled;

    /**
     * 获取主键ID，使用UUID作为主键，确保分布式环境下的唯一性
     *
     * @return id - 主键ID，使用UUID作为主键，确保分布式环境下的唯一性
     */
    public String getId() {
        return id;
    }

    /**
     * 设置主键ID，使用UUID作为主键，确保分布式环境下的唯一性
     *
     * @param id 主键ID，使用UUID作为主键，确保分布式环境下的唯一性
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    /**
     * 获取类型编码，业务层面的唯一标识，用于代码中关联和判断，如：technical、product、meeting、report等
     *
     * @return code - 类型编码，业务层面的唯一标识，用于代码中关联和判断，如：technical、product、meeting、report等
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置类型编码，业务层面的唯一标识，用于代码中关联和判断，如：technical、product、meeting、report等
     *
     * @param code 类型编码，业务层面的唯一标识，用于代码中关联和判断，如：technical、product、meeting、report等
     */
    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
    }

    /**
     * 获取类型名称，用于前端展示，如：技术文档、产品文档、会议纪要、分析报告
     *
     * @return name - 类型名称，用于前端展示，如：技术文档、产品文档、会议纪要、分析报告
     */
    public String getName() {
        return name;
    }

    /**
     * 设置类型名称，用于前端展示，如：技术文档、产品文档、会议纪要、分析报告
     *
     * @param name 类型名称，用于前端展示，如：技术文档、产品文档、会议纪要、分析报告
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    /**
     * 获取类型描述，说明该文档类型的用途和管理规范
     *
     * @return description - 类型描述，说明该文档类型的用途和管理规范
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置类型描述，说明该文档类型的用途和管理规范
     *
     * @param description 类型描述，说明该文档类型的用途和管理规范
     */
    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    /**
     * 获取文档数量，该类型下的文档总数，用于统计和展示
     *
     * @return count - 文档数量，该类型下的文档总数，用于统计和展示
     */
    public Integer getCount() {
        return count;
    }

    /**
     * 设置文档数量，该类型下的文档总数，用于统计和展示
     *
     * @param count 文档数量，该类型下的文档总数，用于统计和展示
     */
    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * 获取分片方式，标识文档内容的切分策略，取值：auto（智能分片，系统自动根据内容结构分析后分片）、fixed（固定长度分片，按指定字符数均匀切分）、paragraph（段落分片，按自然段落边界切分）、recursive（递归分片，按层级结构递归切分）
     *
     * @return chunk_method - 分片方式，标识文档内容的切分策略，取值：auto（智能分片，系统自动根据内容结构分析后分片）、fixed（固定长度分片，按指定字符数均匀切分）、paragraph（段落分片，按自然段落边界切分）、recursive（递归分片，按层级结构递归切分）
     */
    public String getChunkMethod() {
        return chunkMethod;
    }

    /**
     * 设置分片方式，标识文档内容的切分策略，取值：auto（智能分片，系统自动根据内容结构分析后分片）、fixed（固定长度分片，按指定字符数均匀切分）、paragraph（段落分片，按自然段落边界切分）、recursive（递归分片，按层级结构递归切分）
     *
     * @param chunkMethod 分片方式，标识文档内容的切分策略，取值：auto（智能分片，系统自动根据内容结构分析后分片）、fixed（固定长度分片，按指定字符数均匀切分）、paragraph（段落分片，按自然段落边界切分）、recursive（递归分片，按层级结构递归切分）
     */
    public void setChunkMethod(String chunkMethod) {
        this.chunkMethod = chunkMethod == null ? null : chunkMethod.trim();
    }

    /**
     * 获取来源数量，使用该文档类型的数据源数量
     *
     * @return source_count - 来源数量，使用该文档类型的数据源数量
     */
    public Integer getSourceCount() {
        return sourceCount;
    }

    /**
     * 设置来源数量，使用该文档类型的数据源数量
     *
     * @param sourceCount 来源数量，使用该文档类型的数据源数量
     */
    public void setSourceCount(Integer sourceCount) {
        this.sourceCount = sourceCount;
    }

    /**
     * 获取更新时间，文档类型最近一次修改的时间
     *
     * @return update_time - 更新时间，文档类型最近一次修改的时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 设置更新时间，文档类型最近一次修改的时间
     *
     * @param updateTime 更新时间，文档类型最近一次修改的时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 获取类型状态标识，取值：active（正常/激活状态，类型可用）、deleted（已删除状态，不再使用但保留历史数据）
     *
     * @return status - 类型状态标识，取值：active（正常/激活状态，类型可用）、deleted（已删除状态，不再使用但保留历史数据）
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置类型状态标识，取值：active（正常/激活状态，类型可用）、deleted（已删除状态，不再使用但保留历史数据）
     *
     * @param status 类型状态标识，取值：active（正常/激活状态，类型可用）、deleted（已删除状态，不再使用但保留历史数据）
     */
    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    /**
     * 获取来源列表，JSON数组格式，记录使用该类型的数据源信息，如：[{"id":1,"name":"数据源A"},{"id":2,"name":"数据源B"}]
     *
     * @return sources - 来源列表，JSON数组格式，记录使用该类型的数据源信息，如：[{"id":1,"name":"数据源A"},{"id":2,"name":"数据源B"}]
     */
    public String getSources() {
        return sources;
    }

    /**
     * 设置来源列表，JSON数组格式，记录使用该类型的数据源信息，如：[{"id":1,"name":"数据源A"},{"id":2,"name":"数据源B"}]
     *
     * @param sources 来源列表，JSON数组格式，记录使用该类型的数据源信息，如：[{"id":1,"name":"数据源A"},{"id":2,"name":"数据源B"}]
     */
    public void setSources(String sources) {
        this.sources = sources == null ? null : sources.trim();
    }

    /**
     * 获取Ragflow数据集ID
     *
     * @return dataset_id - Ragflow数据集ID
     */
    public String getDatasetId() {
        return datasetId;
    }

    /**
     * 设置Ragflow数据集ID
     *
     * @param datasetId Ragflow数据集ID
     */
    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId == null ? null : datasetId.trim();
    }

    /**
     * 获取是否参与知识库问答检索
     *
     * @return retrieval_enabled - 1-参与，0-不参与
     */
    public Integer getRetrievalEnabled() {
        return retrievalEnabled;
    }

    /**
     * 设置是否参与知识库问答检索
     *
     * @param retrievalEnabled 1-参与，0-不参与（新建记录未传时依赖数据库默认值 1）
     */
    public void setRetrievalEnabled(Integer retrievalEnabled) {
        this.retrievalEnabled = retrievalEnabled;
    }

}