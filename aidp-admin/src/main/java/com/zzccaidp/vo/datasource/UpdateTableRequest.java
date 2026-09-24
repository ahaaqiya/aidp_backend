package com.zzccaidp.vo.datasource;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author zhangtiantian
 * @date 2026/5/6
 */
@Data
public class UpdateTableRequest extends CreateTableRequest {
    @NotBlank(message = "表id不能为空")
    private String tableId;
}
