package com.zzccaidp.service.agent;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.s3.AmazonS3;
import com.zzccaidp.client.AgentScopeClientUtil;
import com.zzccaidp.context.UserInfoContextHolder;
import com.zzccaidp.dao.agent.AgentMessageDO;
import com.zzccaidp.dao.agent.AgentSessionDO;
import com.zzccaidp.dao.system.UserDO;
import com.zzccaidp.mapper.agent.AgentMessageMapper;
import com.zzccaidp.mapper.agent.AgentSessionMapper;
import com.zzccaidp.vo.agent.AgentScopeChatIn;
import com.zzccaidp.vo.ai.DelConversationsIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zzccaidp.common.ConfigPropertieCommon.*;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 16:15
 * @Version: 1.0
 */
@Service
public class AgentScopeService {

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private AgentScopeClientUtil agentScopeClientUtil;

    @Autowired
    private AgentSessionMapper agentSessionMapper;

    @Autowired
    private AgentMessageMapper agentMessageMapper;


    public Flux<String> charMessage(Map<String, Object> map) {
        Map<String, String> userInfo = new HashMap<>();
        UserDO user = UserInfoContextHolder.getUser();
        userInfo.put("userName", user.getUserName());
        userInfo.put("realName", user.getRealName());
        userInfo.put("dept_id", user.getDeptId());
        userInfo.put("dept_name", user.getDeptName());
        map.put("user_info", userInfo);
        return agentScopeClientUtil.webClient(AGENTSCOEP_CHAT_STREAM, map);
    }

    public List<AgentSessionDO> getSessionByUserId(String userId) {
        return agentSessionMapper.listSessionsByUserId(userId);
    }

    public List<AgentMessageDO> getMessageBySessionId(String sessionId) {
        return agentMessageMapper.listMessageBySessionId(sessionId);
    }

    @Transactional
    public void deleteSession(DelConversationsIn delConversations) {
        agentMessageMapper.deleteMessagesBySessionId(delConversations.getConversation_id());
        agentSessionMapper.deleteSessionBySessionId(delConversations.getConversation_id());
    }

    public JSONObject stopConversations(Map<String, String> map) {
        return agentScopeClientUtil.postRestTemplateClient(AGENTSCOEP_STOP_SESSION, map);
    }

    public JSONObject delConversations(Map<String, String> map) {
        return agentScopeClientUtil.deleteRestTemplateClient(AGENTSCOEP_DELETE_SESSION, map);
    }

    public JSONObject conversations(Map<String, String> map) {
        map.put("userId", UserInfoContextHolder.getUserInfo());
        return agentScopeClientUtil.getRestTemplateClient(AGENTSCOEP_SESSION, map);
    }

    public JSONObject messages(Map<String, String> map) {
        return agentScopeClientUtil.getRestTemplateClient(AGENTSCOEP_SESSION_MESSAGE, map);
    }
}
