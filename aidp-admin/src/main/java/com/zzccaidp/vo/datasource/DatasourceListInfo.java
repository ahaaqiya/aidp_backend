package com.zzccaidp.vo.datasource;

import lombok.Data;


/**
 * @author zhangtiantian
 * @date 2026/5/6
 */
@Data
public class DatasourceListInfo {

    private String datasourceId;

    private String datasourceName;

    private String datasourceDescription;

    private String datasourceHost;

    private Integer datasourcePort;

    private String datasourceDatabase;

    private String datasourceUser;

    private String datasourceType;

    private String optionsJson;

    private String isEnabled;

    private String createTime;

    private String updateTime;
}
