package com.zzccaidp.vo.system;

import com.zzccaidp.vo.ResHeader;
import lombok.Data;

import java.util.List;

/**
 * @Description: 角色查询人员列表
 * @Author: WB233500
 * @Createtime: 16:10
 * @Version: 1.0
 */
@Data
public class RoleUserOut extends ResHeader {
    List<RoleUserVO> data;
}
