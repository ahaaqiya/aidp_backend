package com.zzccaidp.vo.datasource;

import com.zzccaidp.vo.ResHeader;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zhangtiantian
 * @date 2026/5/6
 */
@Data
public class CreateDatasourceRequest extends ResHeader {

    @NotBlank(message = "数据源名称不能为空")
    private String datasourceName;

    private String datasourceDescription;

    @NotBlank(message = "数据源地址不能为空")
    private String datasourceHost;

    @NotNull(message = "数据源端口不能为空")
    private Integer datasourcePort;

    @NotBlank(message = "数据源数据库不能为空")
    private String datasourceDatabase;

    private String datasourcePassword;

    @NotBlank(message = "数据源用户不能为空")
    private String datasourceUser;

    @NotBlank(message = "数据源类型不能为空")
    private String datasourceType;

    private String optionsJson;
}
