package com.zzccaidp.vo.system;


import com.zzccaidp.dao.system.PersonInfoDTO;
import com.zzccaidp.vo.ResHeader;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author qiangzhuo
 * @description PersonPageQueryResponse
 * @date 2025/03/24
 */
@EqualsAndHashCode(callSuper = true)
public class PersonPageQueryResponse extends ResHeader {

    /**
     * 人员列表
     */
    private List<TreeNode> personList;

    /**
     * 人员组列表
     */
    private List<TreeNode> personMemberList;

    /**
     * 人员列表
     */
    private List<PersonInfoDTO> personNewList;

    /**
     * 组织机构列表
     */
    private List<TreeNode> orgList;

    public List<TreeNode> getPersonList() {
        return personList;
    }

    public void setPersonList(List<TreeNode> personList) {
        this.personList = personList;
    }

    public List<PersonInfoDTO> getPersonNewList() {
        return personNewList;
    }

    public void setPersonNewList(List<PersonInfoDTO> personNewList) {
        this.personNewList = personNewList;
    }

    public List<TreeNode> getOrgList() {
        return orgList;
    }

    public void setOrgList(List<TreeNode> orgList) {
        this.orgList = orgList;
    }

    public List<TreeNode> getPersonMemberList() {
        return personMemberList;
    }

    public void setPersonMemberList(List<TreeNode> personMemberList) {
        this.personMemberList = personMemberList;
    }
}
