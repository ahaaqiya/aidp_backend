package com.zzccaidp.vo.ai;

import lombok.Getter;
import lombok.Setter;

/**
 * @Description: 会话列表
 * @Author: WB233500
 * @Createtime: 15:40
 * @Version: 1.0
 */
@Getter
@Setter
public class DelConversationsIn {
    /**
     * 用户标识，由开发者定义规则，需保证用户标识在应用内唯一
     */
    private String user;
    /**
     * 会话 ID
     */
    private String conversation_id;
    /**
     * 会话类型，1-dify，2-agentscope
     */
    private String type;

    public String getUrl() {
        return "/" +
                conversation_id;
    }
}
