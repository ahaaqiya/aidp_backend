package com.zzccaidp.vo.system;

import lombok.Data;
import tk.mybatis.mapper.annotation.LogicDelete;

import javax.persistence.Column;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author bades
 */
@Data
public class PermissionIn implements Serializable {

    /**
     * 权限id
     */
    private String id;

    /**
     * 父权限id
     */
    private String pid;

    /**
     * 状态
     */
    private Integer status;
    /**
     * 删除标记
     */
    private Boolean delFlag;

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
     * 菜单层级
     */
    private Integer level;
}
