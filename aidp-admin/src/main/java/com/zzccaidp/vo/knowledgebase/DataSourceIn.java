package com.zzccaidp.vo.knowledgebase;

public class DataSourceIn {
    private String name;
    private String channel;
    private String description;
    private String type;
    private String config;
    private String syncTime;
    private String keyword;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }

    public String getSyncTime() { return syncTime; }
    public void setSyncTime(String syncTime) { this.syncTime = syncTime; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}