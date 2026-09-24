package com.zzccaidp.vo.agent;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 16:10
 * @Version: 1.0
 */
@Data
public class AgentScopeChatIn {
    private String message;
    private String agent_id;
    private String agent_type;
    private String session_id;
    private String user_id;
    private String model;
    private String file_content;
    private List<String> file_names;
    private String title;
    private String deep_thinking;
}
