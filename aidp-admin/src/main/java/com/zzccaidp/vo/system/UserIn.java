package com.zzccaidp.vo.system;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 14:54
 * @Version: 1.0
 */
@Data
public class UserIn implements Serializable {

    private String id;

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
    /**
     * 部门ID
     */
    private String deptId;
}
