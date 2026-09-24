package com.zzccaidp.vo.datasource;

import com.zzccaidp.vo.ResHeader;
import lombok.Data;

import java.util.List;

/**
 * @author zhangtiantian
 * @date 2026/5/6
 */
@Data
public class GetTablePermissionResponse extends ResHeader {
    private String tableId;

    List<TablePermissionInfo> tablePermissionInfoList;
}
