package com.zzccaidp.vo.datasource;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author zhangtiantian
 * @date 2026/5/13
 */
@Data
public class UpdateTableRuleRequest extends CreateTableRuleRequest {
    @NotBlank(message = "规则id不能为空")
    private String ruleId;
}



