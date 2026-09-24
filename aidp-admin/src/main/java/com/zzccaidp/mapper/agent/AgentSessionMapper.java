package com.zzccaidp.mapper.agent;

import com.zzccaidp.dao.agent.AgentSessionDO;
import lombok.Data;
import tk.mybatis.mapper.common.Mapper;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

/**
 * @Description: agentscope智能体历史会话
 * @Author: WB233500
 * @Createtime: 16:53
 * @Version: 1.0
 */
public interface AgentSessionMapper extends Mapper<AgentSessionDO> {
    List<AgentSessionDO> listSessionsByUserId(String userId);

    void deleteSessionBySessionId(String sessionId);
}
