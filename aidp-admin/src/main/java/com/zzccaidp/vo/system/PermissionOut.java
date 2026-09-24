package com.zzccaidp.vo.system;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author bades
 */
@Data
public class PermissionOut implements Serializable {

    /**
     * 权限id
     */
    private String id;

    /**
     * 创建时间
     */
    private LocalDateTime gmtCreate;

    /**
     * 创建用户
     */
    private String gmtCreateUser;

    /**
     * 最后修改时间
     */
    private LocalDateTime gmtModified;

    /**
     * 更新用户
     */
    private String gmtModifiedUser;

    /**
     * 父权限id
     */
    private String pid;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer orderId;

    /**
     * 名称
     */
    private String name;

    /**
     * 类型(1:目录,2:菜单,3:功能)
     */
    private Integer type;

    /**
     * 权限标识
     */
    private String permissionValue;

    /**
     * uri
     */
    private String link;

    /**
     * js模块
     */
    private String moduleJs;

    private String viewParams;

    /**
     * 图标
     */
    private String icon;

    /**
     * 样式
     */
    private String cls;
    /**
     * 删除标记
     */
    private Boolean delFlag;
    /**
     * 菜单层级
     */
    private Integer level;
}
