package com.zzccaidp.vo.ai;

import lombok.Data;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 14:08
 * @Version: 1.0
 */
@Data
public class HistoryMessagesVO {
    private String query;
    private String answer;
    private String conversation_id;
    private String timestamp;
}
