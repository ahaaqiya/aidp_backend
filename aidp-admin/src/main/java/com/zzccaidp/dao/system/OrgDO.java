package com.zzccaidp.dao.system;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 部门表
 * @author zhangtiantian
 * @date 2025/4/1
 */
@Table(name = "hrmsubcompany")
public class OrgDO {

    /*
    部门id
     */
    @Id
    @Column(name = "ID")
    private Integer id;

    /*
    部门名称
     */
    @Column(name = "SUBCOMPANYNAME")
    private String name;

    @Column(name = "SUPSUBCOMID")
    private Integer pid;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }
}
