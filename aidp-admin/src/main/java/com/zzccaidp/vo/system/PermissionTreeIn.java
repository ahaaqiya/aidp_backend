package com.zzccaidp.vo.system;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 17:20
 * @Version: 1.0
 */
@Data
public class PermissionTreeIn implements Serializable {
    List<String> roleIdList;
}
