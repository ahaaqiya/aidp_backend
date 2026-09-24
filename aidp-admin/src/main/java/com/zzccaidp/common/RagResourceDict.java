package com.zzccaidp.common;

import lombok.Getter;

/**
 * @Description: 知识库字典项
 * @Author: WB233500
 * @Createtime: 11:05
 * @Version: 1.0
 */
public class RagResourceDict {

    /*
    知识库状态
     */
    @Getter
    public enum STATUS {
        DISABLED(0, "禁用"),
        ENABLE(1, "启用");

        private Integer status;
        private String desc;

        STATUS(Integer status, String desc) {
            this.status = status;
            this.desc = desc;
        }
    }

    /*
    知识库类型
     */
    @Getter
    public enum KNOWLEDGE_TYPE {
        KNOWLEDGE_BASE(0, "知识库"),
        EXTERNAL_RESEARCH_REPORT(1, "外部研报"),
        OTHERS(2, "其他");

        private Integer status;
        private String desc;

        KNOWLEDGE_TYPE(Integer status, String desc) {
            this.status = status;
            this.desc = desc;
        }
    }

    /*
    知识库同步状态
     */
    @Getter
    public enum SYNC_STATUS {
        IDLE(0, "空闲"),
        SYNC(1, "同步中");

        private Integer status;
        private String desc;

        SYNC_STATUS(Integer status, String desc) {
            this.status = status;
            this.desc = desc;
        }
    }
}
