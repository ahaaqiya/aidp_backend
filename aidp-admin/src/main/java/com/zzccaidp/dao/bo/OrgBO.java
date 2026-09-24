package com.zzccaidp.dao.bo;

import java.io.Serializable;

/**
 * 人员信息
 * @author zhangtiantian
 * @date 2025/3/28
 */
public class OrgBO implements Serializable {

    private static final long serialVersionUID = -6505852491102639801L;
    /*
        机构id
         */
    private Integer id;

    /*
    机构名称
     */
    private String name;

    private Integer pid;

    private String pidName;

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

    public String getPidName() {
        return pidName;
    }

    public void setPidName(String pidName) {
        this.pidName = pidName;
    }
}
