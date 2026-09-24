package com.zzccaidp.dao.agent;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @Description: agentscope智能体会话历史消息
 * @Author: WB233500
 * @Createtime: 16:54
 * @Version: 1.0
 */
@Data
@Table(name = "session_messages")
public class AgentMessageDO {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "session_id")
    private String session_id;
    @Column(name = "message_index")
    private String message_index;
    @Column(name = "role")
    private String role;
    @Column(name = "content")
    private String content;
    @Column(name = "timestamp")
    private Date timestamp;
    @Column(name = "metadata_json")
    private String metadata_json;
}
