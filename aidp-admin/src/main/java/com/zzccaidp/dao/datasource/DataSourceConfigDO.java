package com.zzccaidp.dao.datasource;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author zhangtiantian
 * @date 2026/5/6
 */
@Table(name = "datasource_config")
public class DataSourceConfigDO {

    @Id
    private String id;

    @Column(name = "datasource_name")
    private String datasourceName;

    @Column(name = "datasource_description")
    private String datasourceDescription;

    @Column(name = "datasource_database")
    private String datasourceDatabase;

    @Column(name = "datasource_host")
    private String datasourceHost;

    @Column(name = "datasource_port")
    private Integer datasourcePort;

    @Column(name = "datasource_user")
    private String datasourceUser;

    @Column(name = "datasource_password")
    private String datasourcePassword;

    @Column(name = "datasource_type")
    private String datasourceType;

    @Column(name = "options_json")
    private String optionsJson;

    @Column(name = "is_enabled")
    private String IsEnabled;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

    @Column(name = "create_user")
    private String createUser;

    @Column(name = "update_user")
    private String updateUser;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDatasourceName() {
        return datasourceName;
    }

    public void setDatasourceName(String datasourceName) {
        this.datasourceName = datasourceName;
    }

    public String getDatasourceDescription() {
        return datasourceDescription;
    }

    public void setDatasourceDescription(String datasourceDescription) {
        this.datasourceDescription = datasourceDescription;
    }

    public String getDatasourceDatabase() {
        return datasourceDatabase;
    }

    public void setDatasourceDatabase(String datasourceDatabase) {
        this.datasourceDatabase = datasourceDatabase;
    }

    public String getDatasourceHost() {
        return datasourceHost;
    }

    public void setDatasourceHost(String datasourceHost) {
        this.datasourceHost = datasourceHost;
    }

    public Integer getDatasourcePort() {
        return datasourcePort;
    }

    public void setDatasourcePort(Integer datasourcePort) {
        this.datasourcePort = datasourcePort;
    }

    public String getDatasourceUser() {
        return datasourceUser;
    }

    public void setDatasourceUser(String datasourceUser) {
        this.datasourceUser = datasourceUser;
    }

    public String getDatasourcePassword() {
        return datasourcePassword;
    }

    public void setDatasourcePassword(String datasourcePassword) {
        this.datasourcePassword = datasourcePassword;
    }

    public String getDatasourceType() {
        return datasourceType;
    }

    public void setDatasourceType(String datasourceType) {
        this.datasourceType = datasourceType;
    }

    public String getOptionsJson() {
        return optionsJson;
    }

    public void setOptionsJson(String optionsJson) {
        this.optionsJson = optionsJson;
    }

    public String getIsEnabled() {
        return IsEnabled;
    }

    public void setIsEnabled(String isEnabled) {
        IsEnabled = isEnabled;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }
}
