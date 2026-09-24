package com.zzccaidp.controller.agent;

import com.alibaba.fastjson.JSONObject;
import com.zzccaidp.context.UserInfoContextHolder;
import com.zzccaidp.exception.BusinessException;
import com.zzccaidp.service.agent.AgentScopeService;
import com.zzccaidp.vo.agent.AgentScopeChatIn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * @Description: 只能文档纠错智能体
 * @Author: WB233500
 * @Createtime: 16:03
 * @Version: 1.0
 */
@RestController
@RequestMapping("/agent")
@Slf4j
public class AgentScopeController {
    @Autowired
    private AgentScopeService agentScopeService;

    @PostMapping(value = "/analyzing", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> charMessage(@RequestBody Map<String, Object> map) {
        try {
            return agentScopeService.charMessage(map);
        } catch (BusinessException e) {
            log.error("智能文档纠错失败", e);
            return Flux.just(e.getErrMsg());
        }
    }

    @PostMapping("/messages")
    public JSONObject messages(@RequestBody Map<String, String> map) {
        return agentScopeService.messages(map);
    }

    @PostMapping("/conversations")
    public JSONObject conversations(@RequestBody Map<String, String> map) {
        return agentScopeService.conversations(map);
    }

    @PostMapping("/delConversations")
    public JSONObject delConversations(@RequestBody Map<String, String> map) {
        return agentScopeService.delConversations(map);
    }

    @PostMapping("/stopConversations")
    public JSONObject stopConversations(@RequestBody Map<String, String> map) {
        return agentScopeService.stopConversations(map);
    }

}
