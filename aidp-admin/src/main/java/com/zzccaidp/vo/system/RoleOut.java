package com.zzccaidp.vo.system;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author bades
 */
@Data
public class RoleOut implements Serializable {

    /**
     * 角色id
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
     * 状态
     */
    private Integer status;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;
    /**
     * 删除标记
     */
    private Boolean delFlag;

    private Boolean system;

}
