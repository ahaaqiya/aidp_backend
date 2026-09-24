package com.zzccaidp.service.ai;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zzccaidp.client.DifyClientUtil;
import com.zzccaidp.common.DateUtil;
import com.zzccaidp.common.RedisUtil;
import com.zzccaidp.context.UserInfoContextHolder;
import com.zzccaidp.dao.agent.AgentMessageDO;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.service.agent.AgentScopeService;
import com.zzccaidp.vo.ai.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;

import static com.zzccaidp.common.ConfigPropertieCommon.*;
import static com.zzccaidp.common.DateCommon.DATETIME_PATTERN_DEFAULT;


/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 11:31
 * @Version: 1.0
 */
@Service
@Slf4j
public class AiDialogurService {

    @Autowired
    private RedisUtil redisCommonService;

    @Autowired
    private DifyClientUtil difyClientUtl;

    @Autowired
    private AgentScopeService agentScopeService;

    @Value("${bades.AgentDialogur.appKey}")
    private String appKey;

    public Flux<String> aiChatMessages(ChatMessagesIn chatMessages) {
        //用户权限设置
        setUserPermission(chatMessages);
        return difyClientUtl.webClient(CHAT_MESSAGES, appKey, chatMessages);
    }

    private void setUserPermission(ChatMessagesIn chatMessages) {
        chatMessages.getInputs().put("user_info", JSONObject.toJSONString(UserInfoContextHolder.getUser()));
    }

    public JSONObject aiConversations(ConversationsIn conversations) {
        try {
            return difyClientUtl.getRestTemplateClient(CONVERSATIONS + conversations.getUrl(), appKey, null);
        } catch (Exception e) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(RESULT_CODE, ErrCodeEnum.M0007.getErrCode());
            jsonObject.put(RESULT_MSG, ErrCodeEnum.M0007.getErrMsg());
            return jsonObject;
        }
    }

    public JSONObject aiMessages(HistoryMessagesIn historyMessages) {
        try {
            return difyClientUtl.getRestTemplateClient(MESSAGES + historyMessages.getUrl(), appKey, null);
        } catch (Exception e) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(RESULT_CODE, ErrCodeEnum.M0009.getErrCode());
            jsonObject.put(RESULT_MSG, ErrCodeEnum.M0009.getErrMsg());
            return jsonObject;
        }
    }

    public JSONObject conversationsDelete(DelConversationsIn delConversations) {
        try {
            JSONObject object = new JSONObject();
            object.put("user", delConversations.getUser());
            return difyClientUtl.delRestTemplateClient(CONVERSATIONS + delConversations.getUrl(), appKey, object);
        } catch (Exception e) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(RESULT_CODE, ErrCodeEnum.M0008.getErrCode());
            jsonObject.put(RESULT_MSG, ErrCodeEnum.M0008.getErrMsg());
            return jsonObject;
        }
    }

    public JSONObject stopTask(String taskId) {
        try {
            JSONObject object = new JSONObject();
            object.put("user", UserInfoContextHolder.getUserInfo());
            return difyClientUtl.postRestTemplateClient(String.format(STOP_CHAT, taskId), appKey, object);
        } catch (Exception e) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(RESULT_CODE, ErrCodeEnum.M0008.getErrCode());
            jsonObject.put(RESULT_MSG, ErrCodeEnum.M0008.getErrMsg());
            return jsonObject;
        }
    }

    public void difyHistoryMessage(HistoryMessagesIn historyMessages, HistoryMessagesOut out) {
        JSONObject jsonObject = aiMessages(historyMessages);
        JSONArray jsonArray = jsonObject.getJSONObject("body").getJSONArray("data");
        for (int i = 0; i < jsonArray.size(); i++) {
            HistoryMessagesVO historyMessagesVO = new HistoryMessagesVO();
            historyMessagesVO.setAnswer(jsonArray.getJSONObject(i).getString("answer"));
            historyMessagesVO.setQuery(jsonArray.getJSONObject(i).getString("query"));
            historyMessagesVO.setConversation_id(jsonArray.getJSONObject(i).getString("conversation_id"));
            out.getData().add(historyMessagesVO);
        }
    }

    public void agentHistoryMessage(HistoryMessagesIn historyMessages, HistoryMessagesOut out) {
        List<AgentMessageDO> messageDOList = agentScopeService.getMessageBySessionId(historyMessages.getConversation_id());
        for (int i = 0; i < messageDOList.size(); i++) {
            AgentMessageDO agentMessageDO = messageDOList.get(i);
            if ("assistant".equals(agentMessageDO.getRole())) {
                continue;
            }
            HistoryMessagesVO historyMessagesVO = new HistoryMessagesVO();
            historyMessagesVO.setQuery(agentMessageDO.getContent());
            historyMessagesVO.setAnswer("对话已被用户终止");
            historyMessagesVO.setTimestamp(DateUtil.getDateString(agentMessageDO.getTimestamp()));
            if (i < messageDOList.size() - 1 && messageDOList.get(i + 1).getRole().equals("assistant")) {
                historyMessagesVO.setAnswer(messageDOList.get(i + 1).getContent());
            }
            historyMessagesVO.setConversation_id(historyMessages.getConversation_id());
            out.getData().add(historyMessagesVO);
        }
    }

    public List<ConversationsVO> extracted(ConversationsIn conversations) {
        List<ConversationsVO> list = new ArrayList<>();
        JSONObject object = aiConversations(conversations);
        try {
            JSONArray array = object.getJSONObject("body").getJSONArray("data");
            if (!Objects.isNull(array)) {
                for (int i = 0; i < array.size(); i++) {
                    ConversationsVO vo = new ConversationsVO();
                    vo.setType("1");
                    vo.setId(array.getJSONObject(i).getString("id"));
                    //此处Dify的时间是秒，需要将其扩大1000倍，变成毫秒，才能正确转换成时间戳
                    vo.setCreateTime(DateUtil.longToStringUTC(array.getJSONObject(i).getLong("created_at") * 1000, DATETIME_PATTERN_DEFAULT));
                    vo.setUpdateTime(DateUtil.longToStringUTC(array.getJSONObject(i).getLong("updated_at") * 1000, DATETIME_PATTERN_DEFAULT));
                    vo.setName(array.getJSONObject(i).getString("name"));
                    list.add(vo);
                }
            }
        } catch (Exception exception) {
            log.error("查询Dify历史会话失败", exception);
        }
        try {
            agentScopeService.getSessionByUserId(UserInfoContextHolder.getUserInfo()).forEach(var -> {
                ConversationsVO vo = new ConversationsVO();
                vo.setType("2");
                vo.setCreateTime(DateUtil.getDateString(var.getCreatedAt()));
                vo.setUpdateTime(DateUtil.getDateString(var.getUpdatedAt()));
                vo.setId(var.getSessionId());
                vo.setName(var.getTitle());
                vo.setAgentName("");
                vo.setAgentId(var.getAgentId());
                list.add(vo);
            });
        } catch (Exception exception) {
            log.error("查询Agent历史会话失败", exception);
        }
        //将list按照修改时间排个序
        list.sort(
                Comparator.comparing(
                        ConversationsVO::getUpdateTime,
                        (var1, var2) -> DateUtil.comparing(var1, var2, DATETIME_PATTERN_DEFAULT
                        )
                ).reversed()
        );
        return list;
    }
}
