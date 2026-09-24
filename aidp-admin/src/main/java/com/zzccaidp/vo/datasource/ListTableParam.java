package com.zzccaidp.vo.datasource;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author zhangtiantian
 * @date 2026/5/6
 */
@Data
public class ListTableParam {
    private String tableName;

    @NotBlank
    private String datasourceId;
}