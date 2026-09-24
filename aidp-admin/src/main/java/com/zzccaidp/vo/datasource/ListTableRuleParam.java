package com.zzccaidp.vo.datasource;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author zhangtiantian
 * @date 2026/5/13
 */
@Data
public class ListTableRuleParam {
    private String ruleName;

    @NotBlank(message = "表Id不能为空")
    private String tableId;
}
