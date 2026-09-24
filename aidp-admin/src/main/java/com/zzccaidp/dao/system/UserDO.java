package com.zzccaidp.dao.system;

import lombok.Data;
import lombok.experimental.Accessors;
import tk.mybatis.mapper.annotation.LogicDelete;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 09:03
 * @Version: 1.0
 */

/**
 * @author bades
 */
@Data
@Accessors(chain = true)
@Table(name = "zzccaidp_user")
public class UserDO implements Serializable {


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
     * 账号
     */
    @Column(name = "user_name")
    private String userName;

    /**
     * 机构id
     */
    @Column(name = "org_id")
    private String orgId;

    /**
     * 密码
     */
    @Column(name = "password")
    private String password;

    /**
     * 姓名
     */
    @Column(name = "real_name")
    private String realName;

    /**
     * 用户类别(1-OA,2-AIDP新增)
     */
    @Column(name = "auth_type")
    private String authType;

    /**
     * 小头像
     */
    @Column(name = "avatar")
    private String avatar;

    /**
     * 大头像
     */
    @Transient
    private String bigicon;

    /**
     * 电话
     */
    @Column(name = "phone")
    private String phone;

    /**
     * 邮箱
     */
    @Column(name = "email")
    private String email;

    /**
     * 性别(1男2女)
     */
    @Column(name = "sex")
    private Integer sex;

    /**
     * 状态
     */
    @Column(name = "status")
    private Integer status;


    /**
     * 部门ID
     */
    @Column(name = "dept_id")
    private String deptId;

    @Transient
    private String deptName;
}
