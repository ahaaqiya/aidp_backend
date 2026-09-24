package com.zzccaidp.dao.system;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * 人员基本信息，可继承扩展
 * @author zhangtiantian
 * @date 2025/4/2
 */
@ToString
@Data
public class PersonInfoDTO implements Serializable {

    private static final long serialVersionUID = -6664819981309136173L;

    /*
    id
    */
    private String id;

    /*
    name
    */
    private String name;

    /*
   用户状态
    */
    private String status;

    /*
    工号
     */
    private String workCode;

    /*
    部门Id
     */
    private String deptId;

    /*
    机构Id
     */
    private String orgId;

    /*
    部门名称
     */
    private String deptName;

    /*
    机构名称
     */
    private String orgName;

    /*
    安全级别
     */
    private String secLevel;

    /*
    上级
     */
    private String managerId;
    /*
    上级名称
     */
    private String managerName;

    /*
    账户类型
    */
    private String accountType;

    /*
    小头像
     */
    private String icon;
    /**邮箱*/
    private String email;

    /**办公室电话*/
    private String telephone;

    /**手机*/
    private String mobile;

    /**其他电话*/
    private String mobilecall;

    /**职称*/
    private String jobcall;

    /**大头像*/
    private String bigIcon;

    /**性别*/
    private String sex;

    /**系统语言*/
    private String systemLanguage;

    /**传真*/
    private String fax;

    /**工作地点*/
    private String locationId;

    /**办公室*/
    private String workRoom;


    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getJobcall() {
        return jobcall;
    }

    public void setJobcall(String jobcall) {
        this.jobcall = jobcall;
    }

    public String getBigIcon() {
        return bigIcon;
    }

    public void setBigIcon(String bigIcon) {
        this.bigIcon = bigIcon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWorkCode() {
        return workCode;
    }

    public void setWorkCode(String workCode) {
        this.workCode = workCode;
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getSecLevel() {
        return secLevel;
    }

    public void setSecLevel(String secLevel) {
        this.secLevel = secLevel;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public PersonInfoDTO() {
    }

    public PersonInfoDTO(String id, String name, String workCode, String deptName, String orgName, String icon) {
        this.id = id;
        this.name = name;
        this.workCode = workCode;
        this.deptName = deptName;
        this.orgName = orgName;
        this.icon = icon;
    }

    public PersonInfoDTO(String id, String name, String status, String workCode, String deptId, String orgId, String deptName, String orgName, String secLevel, String managerId, String accountType, String icon, String email) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.workCode = workCode;
        this.deptId = deptId;
        this.orgId = orgId;
        this.deptName = deptName;
        this.orgName = orgName;
        this.secLevel = secLevel;
        this.managerId = managerId;
        this.accountType = accountType;
        this.icon = icon;
        this.email = email;
    }
}
