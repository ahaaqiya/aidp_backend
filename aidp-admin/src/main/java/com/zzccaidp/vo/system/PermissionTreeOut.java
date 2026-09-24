package com.zzccaidp.vo.system;

import com.zzccaidp.vo.ResHeader;
import lombok.Data;

import java.util.List;

/**
 * @Description: TODO
 * @Author: WB233500
 * @Createtime: 17:37
 * @Version: 1.0
 */
@Data
public class PermissionTreeOut extends ResHeader {
    List<PermissionTree> permissionTreeList;
}
