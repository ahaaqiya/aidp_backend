package com.zzccaidp.vo.ai;

import com.zzccaidp.vo.SearchVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author bades
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class TransLogSearchReq extends SearchVO {

	private Long id;

	private String startTime;

	private String endTime;

	private String ruleId;
	private String transFlowId;
	private String chl;
}
