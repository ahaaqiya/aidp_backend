package com.zzccaidp.vo.datasource;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author zhangtiantian
 * @date 2026/5/6
 */
@Data
public class UpdateDatasourceRequest extends CreateDatasourceRequest {

    @NotBlank(message = "数据源Id不能未空")
    private String datasourceId;
}
