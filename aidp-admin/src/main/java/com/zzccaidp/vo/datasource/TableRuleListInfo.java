package com.zzccaidp.vo.datasource;

import lombok.Data;

/**
 * @author zhangtiantian
 * @date 2026/5/13
 */
@Data
public class TableRuleListInfo {

    private String ruleId;

    private String ruleName;

    private String ruleDescription;

    private String tableId;

    private String datasourceId;

    private String ruleCondition;

    private String createTime;

    private String updateTime;
}
