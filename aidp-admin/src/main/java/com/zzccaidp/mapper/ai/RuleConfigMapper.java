package com.zzccaidp.mapper.ai;

import com.zzccaidp.dao.ai.RuleConfigDO;
import tk.mybatis.mapper.common.Mapper;


/**
 * 规则配置
 *
 * @author bades
 */
public interface RuleConfigMapper extends Mapper<RuleConfigDO> {
    void updateRuleConfigDOByRuleId(RuleConfigDO ruleConfig);

    void updateRuleConfigDOByTemplateId(RuleConfigDO ruleConfig);

    RuleConfigDO getRuleConfigByRuleId(String ruleId);
}
