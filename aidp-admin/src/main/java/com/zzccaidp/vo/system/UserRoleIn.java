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
public class UserRoleIn {
    /**
     * 用户id
     */
    private String id;
    /**
     * 角色id
     */
    private List<String> roles;
}
