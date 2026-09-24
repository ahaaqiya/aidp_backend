package com.zzccaidp.vo.datasource;

import com.zzccaidp.vo.ReqHeader;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author zhangtiantian
 * @date 2026/5/13
 */
@Data
public class CreateTableRuleRequest extends ReqHeader {
    @NotBlank(message = "表Id不能为空")
    private String tableId;

    @NotBlank(message = "数据源id不能为空")
    private String datasourceId;

    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    @NotBlank(message = "规则条件不能为空")
    private String ruleCondition;

    private String ruleDescription;
}
