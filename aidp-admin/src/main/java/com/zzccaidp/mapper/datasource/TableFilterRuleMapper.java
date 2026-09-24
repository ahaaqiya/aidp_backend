package com.zzccaidp.mapper.datasource;

import com.zzccaidp.dao.datasource.TableFilterRuleDO;
import com.zzccaidp.vo.datasource.ListTableRuleParam;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author zhangtiantian
 * @date 2026/5/13
 */
public interface TableFilterRuleMapper extends Mapper<TableFilterRuleDO> {
    List<TableFilterRuleDO> listTableRule(ListTableRuleParam data);
}
