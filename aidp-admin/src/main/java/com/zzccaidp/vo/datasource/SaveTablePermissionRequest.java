package com.zzccaidp.vo.datasource;

import com.zzccaidp.vo.ReqHeader;
import lombok.Data;

import java.util.List;

/**
 * @author zhangtiantian
 * @date 2026/5/6
 */
@Data
public class SaveTablePermissionRequest extends ReqHeader {
    private String datasourceId;

    private String tableId;

    private List<TablePermissionInfo> tablePermissionInfoList;
}
