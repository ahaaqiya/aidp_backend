package com.zzccaidp.dao.ai;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author bades
 */
@Data
@Accessors(chain = true)
@Table(name = "ai_line_log")
public class TransLog implements Serializable {

	@Id
	@Column(name = "id")
	private Long id;

	/**
	 * 流水编号
 	 */
	@Column(name = "trans_flow_id")
	private String transFlowId;

	/**
	 * 调用方渠道
	 */
	@Column(name = "chl")
	private String chl;

	/**
	 * 返回码
	 */
	@Column(name = "errcode")
	private String errcode;

	/**
	 * 返回信息
	 */
	@Column(name = "errmsg")
	private String errmsg;

	/**
	 * 请求开始时间
 	 */
	@Column(name = "start_time")
	private LocalDateTime startTime;

	/**
	 * 请求結束时间
	 */
	@Column(name = "end_time")
	private LocalDateTime endTime;

	/**
	 * 请求耗时
 	 */
	@Column(name = "count_time")
	private Long countTime;

	/**
	 * 调用方IP
 	 */
	@Column(name = "server_address")
	private String serverAddress;

	/**
	 * 返回方IP
	 */
	@Column(name = "remote_address")
	private String remoteAddress;

	/**
	 * 文件路径
	 */
	@Column(name = "file_path")
	private String filePath;

	/**
	 * 场景服务号
	 */
	@Column(name = "rule_id")
	private String ruleId;

	/**
	 * 请求报文
	 */
	@Column(name = "req_body")
	private String reqBody;

	/**
	 * 响应报文
	 */
	@Column(name = "res_body")
	private String resBody;

	public static final String F_ID = "id";
	public static final String F_START_TIME = "startTime";
	public static final String F_RULD_ID = "ruleId";
	public static final String F_CHL = "chl";
	public static final String F_TRANS_FLOW_ID = "transFlowId";
}
