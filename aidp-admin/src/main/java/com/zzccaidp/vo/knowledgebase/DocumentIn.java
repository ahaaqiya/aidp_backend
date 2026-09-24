package com.zzccaidp.vo.knowledgebase;

import java.io.Serializable;
import java.util.List;

public class DocumentIn implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文档ID
     */
    private Long id;

    /**
     * 文档名称
     */
    private String name;

    /**
     * 文档类型编码
     */
    private String docType;

    /**
     * 数据源ID
     */
    private Integer dataSourceId;

    /**
     * 来源状态
     */
    private String sourceStatus;

    /**
     * 任务状态
     */
    private String taskStatus;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 关键字搜索
     */
    private String keyword;

    /**
     * 启用状态
     */
    private Boolean switchStatus;

    /**
     * 可见范围
     */
    private List<String> visibility;

    /**
     * 向量启用状态
     */
    private String vectorEnabled;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public Integer getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(Integer dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getSourceStatus() {
        return sourceStatus;
    }

    public void setSourceStatus(String sourceStatus) {
        this.sourceStatus = sourceStatus;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Boolean getSwitchStatus() {
        return switchStatus;
    }

    public void setSwitchStatus(Boolean switchStatus) {
        this.switchStatus = switchStatus;
    }

    public List<String> getVisibility() {
        return visibility;
    }

    public void setVisibility(List<String> visibility) {
        this.visibility = visibility;
    }

    public String getVectorEnabled() {
        return vectorEnabled;
    }

    public void setVectorEnabled(String vectorEnabled) {
        this.vectorEnabled = vectorEnabled;
    }
}
