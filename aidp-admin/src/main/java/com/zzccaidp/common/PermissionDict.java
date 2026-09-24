package com.zzccaidp.common;

import lombok.Getter;

/**
 * @Description: 菜单模块字典项码值
 * @Author: WB233500
 * @Createtime: 14:16
 * @Version: 1.0
 */
public class PermissionDict {

    /*
    删除标志
     */
    @Getter
    public enum DEF_FLAG {
        DISABLED(0, "未删除"),
        ENABLE(1, "删除");

        private Integer status;
        private String desc;

        DEF_FLAG(Integer status, String desc) {
            this.status = status;
            this.desc = desc;
        }
    }

    /*
    菜单状态
     */
    @Getter
    public enum STATUS {
        ENABLE(0, "启用"),
        DISABLED(1, "禁用");

        private Integer status;
        private String desc;

        STATUS(Integer status, String desc) {
            this.status = status;
            this.desc = desc;
        }
    }

    /*
    菜单类型
     */
    @Getter
    public enum TYPE {
        DIRECTORY(1, "目录"),
        MENU(2, "菜单"),
        FUNCTIONS(3, "功能");

        private Integer status;
        private String desc;

        TYPE(Integer status, String desc) {
            this.status = status;
            this.desc = desc;
        }
    }
}
