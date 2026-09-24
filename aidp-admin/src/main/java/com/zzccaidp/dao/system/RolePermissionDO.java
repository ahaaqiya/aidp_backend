package com.zzccaidp.dao.system;

import lombok.Data;
import lombok.experimental.Accessors;
import tk.mybatis.mapper.annotation.LogicDelete;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author bades
 */
@Data
@Accessors(chain = true)
@Table(name = "zzccaidp_role_permission")
public class RolePermissionDO implements Serializable {

    @Id
    @Column(name = "id")
    private String id;

    /**
     * 创建时间
     */
    @Column(name = "gmt_create")
    private LocalDateTime gmtCreate;

    /**
     * 创建用户
     */
    @Column(name = "gmt_create_user")
    private String gmtCreateUser;

    /**
     * 最后修改时间
     */
    @Column(name = "gmt_modified")
    private LocalDateTime gmtModified;

    /**
     * 更新用户
     */
    @Column(name = "gmt_modified_user")
    private String gmtModifiedUser;

    /**
     * 角色id
     */
    @Column(name = "role_id")
    private String roleId;

    /**
     * 权限id
     */
    @Column(name = "permission_id")
    private String permissionId;

    /**
     * 权限半选全选菜单状态（0-半选；1-全选）
     */
    @Column(name = "status")
    private String status;
}
