package com.zzccaidp.vo.system;

import com.zzccaidp.vo.ResHeader;
import lombok.Data;

import java.util.List;

/**
 * @Description: TODO
 * @Author: WB233500
 * @Createtime: 17:40
 * @Version: 1.0
 */
@Data
public class PermissionIdListOut extends ResHeader {
    private List<String> permissionIdList;
}
