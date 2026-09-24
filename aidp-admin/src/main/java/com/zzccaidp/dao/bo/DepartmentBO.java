package com.zzccaidp.dao.bo;

import java.io.Serializable;

/**
 * 人员信息
 * @author zhangtiantian
 * @date 2025/3/28
 */
public class DepartmentBO implements Serializable {

    private static final long serialVersionUID = -1220674452522092035L;

    /*
       部门id
        */
    private String id;

    /*
    部门名称
     */
    private String name;

    /*
     部门简称
    */
    private String shortName;

    /*
    机构id
     */
    private String pid;

    /*
    机构名字
     */
    private String pidName;
    /*
    上级部门id
     */
    private String supDeptId;

    /*
    在职员工数
     */
    private String employees;
    /**
     * 离职员工数
     */
    private String unemployees;
    /**
     * 显示顺序
     */
    private String showorder;

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getShoworder() {
        return showorder;
    }

    public void setShoworder(String showorder) {
        this.showorder = showorder;
    }

    public String getEmployees() {
        return employees;
    }

    public void setEmployees(String employees) {
        this.employees = employees;
    }

    public String getUnemployees() {
        return unemployees;
    }

    public void setUnemployees(String unemployees) {
        this.unemployees = unemployees;
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

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPidName() {
        return pidName;
    }

    public void setPidName(String pidName) {
        this.pidName = pidName;
    }

    public String getSupDeptId() {
        return supDeptId;
    }

    public void setSupDeptId(String supDeptId) {
        this.supDeptId = supDeptId;
    }
}
