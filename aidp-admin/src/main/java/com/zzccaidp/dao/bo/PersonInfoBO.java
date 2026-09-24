package com.zzccaidp.dao.bo;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * 人员信息
 * @author zhangtiantian
 * @date 2025/3/28
 */
@ToString
@Data
public class PersonInfoBO implements Serializable {

    private static final long serialVersionUID = -5561651919153714951L;

    /*
    id
    */
    private String id;

    /*
    name
    */
    private String name;

    private String pinyin;

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

    /**大头像fileid*/
    private String resourceimageid;

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

    /**创建时间*/
    private String createDate;


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

    public String getResourceimageid() {
        return resourceimageid;
    }

    public void setResourceimageid(String resourceimageid) {
        this.resourceimageid = resourceimageid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }
}
