package com.zzccaidp.dao.agent;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @Description: agentscope智能体历史会话
 * @Author: WB233500
 * @Createtime: 16:53
 * @Version: 1.0
 */
@Data
@Table(name = "session")
public class AgentSessionDO {
    @Id
    @Column(name = "session_id")
    private String sessionId;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "agent_id")
    private String agentId;
    @Column(name = "agent_type")
    private String agentType;
    @Column(name = "title")
    private String title;
    @Column(name = "status")
    private String status;
    @Column(name = "model")
    private String model;
    @Column(name = "context_summary")
    private String contextSummary;
    @Column(name = "total_tokens")
    private Integer totalTokens;
    @Column(name = "created_at")
    private Date createdAt;
    @Column(name = "updated_at")
    private Date updatedAt;
}
