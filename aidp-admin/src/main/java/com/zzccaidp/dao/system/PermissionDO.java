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
@Table(name = "zzccaidp_permission")
public class PermissionDO implements Serializable {

    /**
     * 权限id
     */
    @Id
    @Column(name = "id")
    private String id;

    /**
     * 删除标记
     */
    @LogicDelete
    @Column(name = "del_flag")
    private Boolean delFlag;

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
     * 父权限id
     */
    @Column(name = "pid")
    private String pid;

    /**
     * 状态
     */
    @Column(name = "status")
    private Integer status;

    /**
     * 排序
     */
    @Column(name = "order_id")
    private Integer orderId;

    /**
     * 名称
     */
    @Column(name = "name")
    private String name;

    /**
     * 类型(1:目录,2:菜单,3:功能)
     */
    @Column(name = "type")
    private Integer type;

    /**
     * 权限标识
     */
    @Column(name = "permission_value")
    private String permissionValue;

    /**
     * link
     */
    @Column(name = "link")
    private String link;

    /**
     * js模块
     */
    @Column(name = "module_js")
    private String moduleJs;

    @Column(name = "view_params")
    private String viewParams;

    /**
     * 图标
     */
    @Column(name = "icon")
    private String icon;

    /**
     * 样式
     */
    @Column(name = "cls")
    private String cls;
    /**
     * 菜单层级
     */
    @Column(name = "level")
    private Integer level;
}
