package com.zzccaidp.vo.ai;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author bades
 */
@Data
@Accessors(chain = true)
public class TransLogPageRes {


	private Long id;

	/**
	 * 流水编号
	 */
	private String transFlowId;

	/**
	 * 调用方渠道
	 */
	private String chl;

	/**
	 * 返回码
	 */
	private String errcode;

	/**
	 * 返回信息
	 */
	private String errmsg;

	/**
	 * 请求开始时间
	 */
	private String startTime;

	/**
	 * 请求結束时间
	 */
	private String endTime;

	/**
	 * 请求耗时
	 */
	private int countTime;

	/**
	 * 调用方IP
	 */
	private String serverAddress;

	/**
	 * 返回方IP
	 */
	private String remoteAddress;

	/**
	 * 文件路径
	 */
	private String filePath;

	/**
	 * 场景服务号
	 */
	private String ruleId;

	/**
	 * 请求报文
	 */
	private String reqBody;

	/**
	 * 响应报文
	 */
	private String resBody;

}
