package com.zzccaidp.vo.system;

import com.zzccaidp.vo.ResHeader;
import lombok.Data;

import java.util.List;

/**
 * @Description: 用户登录出参
 * @Author: WB233500
 * @Createtime: 09:30
 * @Version: 1.0
 */
@Data
public class UserLoginOut extends ResHeader {
    /**
     * token
     */
    private String token;
    /**
     * 用户id
     */
    private String userid;
    /**
     * 登录名称
     */
    private String username;
    /**
     * 用户描述
     */
    private String userdesc;
    /**
     * 用户姓名
     */
    private String realName;
    /**
     * 角色列表
     */
    private List<String> roleIds;
    /**
     * 角色列表
     */
    private List<PermissionTree> menuList;

    private String orgId;
    private String deptId;
    private String authType;
    private Integer sex;
    private String deptName;

    /*
   小头像
    */
    private String icon;
    /**大头像fileid*/
    private String bigicon;
}
