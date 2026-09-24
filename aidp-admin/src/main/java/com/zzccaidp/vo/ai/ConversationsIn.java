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
public class ConversationsIn {
    /**
     * 用户标识，由开发者定义规则，需保证用户标识在应用内唯一
     */
    private String user;
    /**
     * （选填）当前页最后面一条记录的 ID，默认 null
     */
    private String lastId = "";
    /**
     * （选填）一次请求返回多少条记录，默认 20 条，最大 100 条，最小 1 条。
     */
    private int limit = 20;
    /**
     * （选填）排序字段，默认 -updated_at(按更新时间倒序排列)
     * 可选值：created_at, -created_at, updated_at, -updated_at
     * 字段前面的符号代表顺序或倒序，-代表倒序
     */
    private String sortBy ="-created_at";

    public String getUrl() {
        return "?user=" +
                user +
                "&limit=" +
                limit +
                "&sort_by=" +
                sortBy +
                "&lastId=" +
                lastId;
    }
}
