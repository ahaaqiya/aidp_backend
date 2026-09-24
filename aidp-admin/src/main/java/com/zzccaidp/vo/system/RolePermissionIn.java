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
public class RolePermissionIn {
    /**
     * 角色id
     */
    private String id;
    /**
     * 权限全选id
     */
    private List<String> permissions;
    /**
     * 权限半选id
     */
    private List<String> halfPermissions;
}
