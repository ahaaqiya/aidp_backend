package com.zzccaidp.vo.ai;

import lombok.Getter;
import lombok.Setter;

/**
 * @Description: 会话历史消息
 * @Author: WB233500
 * @Createtime: 15:44
 * @Version: 1.0
 */
@Getter
@Setter
public class HistoryMessagesIn {
    /**
     * 用户标识，由开发者定义规则，需保证用户标识在应用内唯一
     */
    private String user;
    /**
     * 当前页第一条聊天记录的 ID，默认 null
     */
    private String first_id = "";
    /**
     * （选填）一次请求返回多少条记录，默认 20 条，最大 100 条，最小 1 条。
     */
    private int limit = 20;
    /**
     * 会话 ID
     */
    private String conversation_id;
    /**
     * 会话类型1-dify，2-agentscope
     */
    private String type;

    public String getUrl() {
        return "?user=" +
                user +
                "&limit=" +
                limit +
                "&first_id=" +
                first_id +
                "&conversation_id=" +
                conversation_id;
    }
}
