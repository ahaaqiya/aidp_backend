package com.zzccaidp.vo.system;

import lombok.Data;

/**
 * @Description: TODO
 * @Author: WB233500
 * @Createtime: 15:31
 * @Version: 1.0
 */
@Data
public class RoleUserVO {
    private String id;//  人ID
    private String label;// 姓名
    private String pidName;// 所属部门
    private String workCode;// 工号
}
