package com.zzccaidp.vo.system;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 14:54
 * @Version: 1.0
 */
@Data
public class UserOut implements Serializable {

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
     * 账号
     */
    private String userName;

    /**
     * 机构id
     */
    private String orgId;

    /**
     * 密码
     */
    private String password;

    /**
     * 姓名
     */
    private String realName;

    /**
     * 认证方式(1统一认证,2用户密码)
     */
    private Integer authType;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 头像
     */
    private String bigicon;
    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 性别(1男2女)
     */
    private Integer sex;

    /**
     * 状态
     */
    private Integer status;
    /**
     * 删除标记
     */
    private Boolean delFlag;

    private String deptId;

    private String deptName;
    /**
     * 用户所有角色
     */
    private List<RoleOut> roleOutList;
}
