package com.zzccaidp.vo.system;

import com.zzccaidp.vo.ResHeader;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 菜单树
 * @Author: WB233500
 * @Createtime: 16:37
 * @Version: 1.0
 */
@Data
public class PermissionTree {
    private String id;
    private String pid;
    private Integer status;
    private Integer orderId;
    private String name;
    private Integer type;
    private String permissionValue;
    private String link;
    private String moduleJs;
    private String viewParams;
    private String icon;
    private String cls;
    private Boolean delFlag;
    private Integer level;
    /**
     * 节点
     */
    List<PermissionTree> children = new ArrayList<>();
}
