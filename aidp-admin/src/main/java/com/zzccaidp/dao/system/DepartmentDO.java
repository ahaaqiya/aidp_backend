package com.zzccaidp.dao.system;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 部门表
 * @author zhangtiantian
 * @date 2025/4/1
 */
@Table(name = "hrmdepartment")
public class DepartmentDO {

    /*
    部门id
     */
    @Id
    @Column(name = "ID")
    private String id;

    /*
    部门名称
     */
    @Column(name = "DEPARTMENTNAME")
    private String name;
    /*
    部门名称
     */
    @Column(name = "DEPARTMENTMARK")
    private String shortName;

    /*
    机构id
     */
    @Column(name = "SUBCOMPANYID1")
    private String pid;

    /*
    上级部门Id
     */
    @Column(name = "SUPDEPID")
    private String supDeptId;

    /**
     * 显示顺序
     */
    @Column(name = "SHOWORDER")
    private Float showorder;

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Float getShoworder() {
        return showorder;
    }

    public void setShoworder(Float showorder) {
        this.showorder = showorder;
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

    public String getSupDeptId() {
        return supDeptId;
    }

    public void setSupDeptId(String supDeptId) {
        this.supDeptId = supDeptId;
    }
}
