package com.zzccaidp.mapper.agent;

import com.zzccaidp.dao.agent.AgentMessageDO;
import lombok.Data;
import tk.mybatis.mapper.common.Mapper;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;

/**
 * @Description: agentscope智能体会话历史消息
 * @Author: WB233500
 * @Createtime: 16:54
 * @Version: 1.0
 */
public interface AgentMessageMapper extends Mapper<AgentMessageDO> {
    List<AgentMessageDO> listMessageBySessionId(String sessionId);

    void deleteMessagesBySessionId(String sessionId);
}
