package com.zzccaidp.vo.ai;

import lombok.Data;

import java.util.Date;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 09:56
 * @Version: 1.0
 */
@Data
public class ConversationsVO {
    private String name;
    private String id;
    /**回话类型，1-dify，2-agent scope*/
    private String type;
    private String createTime;
    private String updateTime;
    private String agentId;
    private String agentName;
}
