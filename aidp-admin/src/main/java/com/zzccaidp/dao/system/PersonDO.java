package com.zzccaidp.dao.system;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/** 人员表
 * @author zhangtiantian
 * @date 2025/3/28
 */
@Table(name = "hrmresource")
public class PersonDO {
    /*
    id
     */
    @Id
    @Column(name = "ID")
    private Integer id;

    /*
    工号
     */
    @Column(name = "WORKCODE")
    private String workCode;

    /*
   姓名
    */
    @Column(name = "LASTNAME")
    private String name;

    /*
    部门Id
     */
    @Column(name = "DEPARTMENTID")
    private String deptId;

    /*
    机构Id
     */
    @Column(name = "SUBCOMPANYID1")
    private String orgId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getWorkCode() {
        return workCode;
    }

    public void setWorkCode(String workCode) {
        this.workCode = workCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
