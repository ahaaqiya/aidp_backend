package com.zzccaidp.controller.ai;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zzccaidp.common.DateUtil;
import com.zzccaidp.context.UserInfoContextHolder;
import com.zzccaidp.service.agent.AgentScopeService;
import com.zzccaidp.service.ai.AiDialogurService;
import com.zzccaidp.service.system.SystemParamsService;
import com.zzccaidp.util.ResUtils;
import com.zzccaidp.vo.ResHeader;
import com.zzccaidp.vo.ai.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.*;


/**
 * @Description: 调用Dify入口，负责给每次请求加上用户信息。方便后续用户权限控制
 * @Author: WB233500
 * @Createtime: 10:57
 * @Version: 1.0
 */

@RestController
@RequestMapping("/ai")
@Slf4j
public class AiDialogurController {


    @Autowired
    private AiDialogurService aiDialogurService;

    @Autowired
    private AgentScopeService agentScopeService;

    @Autowired
    private SystemParamsService systemParamsService;


    /**
     * 开始对话，如果不传conversation_id相当于是新的一轮对话
     *
     * @param chatMessages
     * @return
     */
    @PostMapping(value = "/chat-messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> aiChatMessages(@RequestBody ChatMessagesIn chatMessages) {
        if (chatMessages.getInputs() == null) {
            chatMessages.setInputs(new JSONObject());
        }
        chatMessages.setUser(UserInfoContextHolder.getUserInfo());
        return aiDialogurService.aiChatMessages(chatMessages);
    }

    /**
     * 获取历史对话
     *
     * @param conversations
     * @return
     */
    @PostMapping("/conversations")
    public ConversationsOut aiConversations(@RequestBody ConversationsIn conversations) {
        ConversationsOut out = new ConversationsOut();
        out.setData(new ArrayList<>());
        out.setSuccessCode();
        conversations.setUser(UserInfoContextHolder.getUserInfo());
        try {
            out.setData(aiDialogurService.extracted(conversations));
        } catch (Exception e) {
            log.error("查询历史会话失败", e);
        }
        return out;
    }


    /**
     * 获取当前对话历史消息
     *
     * @param historyMessages
     * @return
     */
    @PostMapping("/messages")
    public HistoryMessagesOut aiMessages(@RequestBody HistoryMessagesIn historyMessages) {
        HistoryMessagesOut out = new HistoryMessagesOut();
        out.setSuccessCode();
        out.setData(new ArrayList<>());
        historyMessages.setUser(UserInfoContextHolder.getUserInfo());
        if ("1".equals(historyMessages.getType())) {
            aiDialogurService.difyHistoryMessage(historyMessages, out);
        } else if ("2".equals(historyMessages.getType())) {
            aiDialogurService.agentHistoryMessage(historyMessages, out);
        }
        return out;
    }


    @PostMapping("/delConversations")
    public ResHeader conversationsDelete(@RequestBody DelConversationsIn delConversations) {
        ResHeader out = new ResHeader();
        delConversations.setUser(UserInfoContextHolder.getUserInfo());
        try {
            if ("1".equals(delConversations.getType())) {
                aiDialogurService.conversationsDelete(delConversations);
            } else {
                agentScopeService.deleteSession(delConversations);
            }
            out.setSuccessCode();
        }catch (Exception e){
            log.error("会话删除失败，{}",JSONObject.toJSONString(delConversations),e);
            out.setErrorCode();
        }
        return out;
    }


    @PostMapping("/chat-messages/{taskId}/stop")
    public JSONObject stopTask(@PathVariable String taskId) {
        return aiDialogurService.stopTask(taskId);
    }

    @PostMapping("/thinkConfig")
    public ThinkConfigResponse getThinkConfig() {
        return ResUtils.success(new ThinkConfigResponse(systemParamsService.getSysParamValue("LLM_THINK_ENABLE", "1")));
    }
}
