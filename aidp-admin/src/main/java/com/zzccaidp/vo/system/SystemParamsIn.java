package com.zzccaidp.vo.system;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 10:18
 * @Version: 1.0
 */
@Data
public class SystemParamsIn implements Serializable {
    private String id;
    private String paramsKey;
    private String paramsValue;
    private String remark;
    /**
     * 状态0-停用，1-启用
     */
    private String paramsStart;
}
