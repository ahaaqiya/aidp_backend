package com.zzccaidp.vo.ai;

import com.zzccaidp.vo.ResHeader;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 规则配置表
 * </p>
 *
 * @author WB255485
 * @since 2025-11-19
 */
@Data
@Accessors(chain = true)
public class RuleConfigListVo extends ResHeader implements Serializable {

    List<RuleConfigInfoVo> ruleConfigListVo;

}
