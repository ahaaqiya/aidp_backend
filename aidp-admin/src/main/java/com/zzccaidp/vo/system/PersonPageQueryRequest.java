package com.zzccaidp.vo.system;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @author qiangzhuo
 * @description PersonPageQueryRequest
 * @date 2025/03/24
 */
@EqualsAndHashCode(callSuper = false)
public class PersonPageQueryRequest implements Serializable {

    /**
     * 搜索条件
     */
    private String personNameList;

    /**
     * 当前登录人员部门ID
     */
    private String currentLoginDeptid;

    /**
     * 部门ID List
     */
    private List<String> deptidList;

    /**
     * 工号 List
     */
    private List<String> workcodeList;

    /**
     * 最近查询个数
     */
    private Integer queryCountt;

    public Integer getQueryCountt() {
        return queryCountt;
    }

    public void setQueryCountt(Integer queryCountt) {
        this.queryCountt = queryCountt;
    }

    public String getPersonNameList() {
        return personNameList;
    }

    public void setPersonNameList(String personNameList) {
        this.personNameList = personNameList;
    }

    public String getCurrentLoginDeptid() {
        return currentLoginDeptid;
    }

    public void setCurrentLoginDeptid(String currentLoginDeptid) {
        this.currentLoginDeptid = currentLoginDeptid;
    }

    public List<String> getDeptidList() {
        return deptidList;
    }

    public void setDeptidList(List<String> deptidList) {
        this.deptidList = deptidList;
    }

    public List<String> getWorkcodeList() {
        return workcodeList;
    }

    public void setWorkcodeList(List<String> workcodeList) {
        this.workcodeList = workcodeList;
    }
}
