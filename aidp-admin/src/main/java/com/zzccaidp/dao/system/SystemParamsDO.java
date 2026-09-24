package com.zzccaidp.dao.system;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 10:18
 * @Version: 1.0
 */
@Data
@Table(name = "zzccaidp_system_params")
public class SystemParamsDO implements Serializable {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "params_key")
    private String paramsKey;
    @Column(name = "params_value")
    private String paramsValue;
    @Column(name = "remark")
    private String remark;
    /**
     * 状态0-停用，1-启用
     */
    @Column(name = "params_start")
    private String paramsStart;
    @Column(name = "params_type")
    private String paramsType;
}
