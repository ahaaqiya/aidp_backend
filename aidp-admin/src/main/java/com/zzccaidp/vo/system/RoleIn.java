package com.zzccaidp.vo.system;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author bades
 */
@Data
public class RoleIn implements Serializable {

    /**
     * 角色id
     */
    private String id;

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
