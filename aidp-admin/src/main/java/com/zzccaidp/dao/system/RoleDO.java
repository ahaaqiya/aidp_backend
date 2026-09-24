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
@Table(name = "zzccaidp_role")
public class RoleDO implements Serializable {

    /**
     * 角色id
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
     * 状态
     */
    @Column(name = "status")
    private Integer status;

    /**
     * 名称
     */
    @Column(name = "name")
    private String name;

    /**
     * 描述
     */
    @Column(name = "description")
    private String description;


    @Column(name = "system_flag")
    private String system;
}
