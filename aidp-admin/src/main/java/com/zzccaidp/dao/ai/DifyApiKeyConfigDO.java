package com.zzccaidp.dao.ai;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author zhangtiantian
 * @date 2026/4/13
 */
@Table(name = "dify_api_key_config")
public class DifyApiKeyConfigDO {

    @Id
    private String id;

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "key_name")
    private String keyName;

    /**
     * 描述
     */
    @Column(name = "key_desc")
    private String keyDesc;

    /**
     * 类型(业务类型)
     */
    @Column(name = "key_type")
    private String keyType;


    /**
     * dify应用类型
     */
    @Column(name = "dify_app_type")
    private String difyAppType;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 创建用户
     */
    @Column(name = "create_user")
    private String createUser;

    /**
     * 最后修改时间
     */
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 更新用户
     */
    @Column(name = "update_user")
    private String updateUser;

    /**
     * 是否启用
     */
    @Column(name = "is_enabled")
    private String isEnabled;

    @Column(name = "is_default")
    private String isDefault;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getKeyDesc() {
        return keyDesc;
    }

    public void setKeyDesc(String keyDesc) {
        this.keyDesc = keyDesc;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String type) {
        this.keyType = type;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    public String getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(String isEnabled) {
        this.isEnabled = isEnabled;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getDifyAppType() {
        return difyAppType;
    }

    public void setDifyAppType(String difyAppType) {
        this.difyAppType = difyAppType;
    }

    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault;
    }
}
