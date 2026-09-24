package com.zzccaidp.vo.system;

import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 09:36
 * @Version: 1.0
 */
@Data
public class RoleUserIn {
    /**
     * 角色id
     */
    private String id;
    /**
     * 用户id
     */
    private List<String> userIdList;
}
