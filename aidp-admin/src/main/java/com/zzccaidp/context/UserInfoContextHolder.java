package com.zzccaidp.context;

import com.zzccaidp.dao.system.UserDO;

import java.util.Objects;

/**
 * @Description: 线程上下文保存用户信息。目前只保存了工号
 * @Author: WB233500
 * @Createtime: 11:25
 * @Version: 1.0
 */
public final class UserInfoContextHolder {
    private static final ThreadLocal<UserDO> CONTEXT_HOLDER = new ThreadLocal<>();

    private UserInfoContextHolder() {

    }

    public static void addUserInfo(UserDO value) {
        CONTEXT_HOLDER.set(value);
    }

    public static UserDO getUser() {
        return CONTEXT_HOLDER.get();
    }

    public static String getUserInfo() {
        if (Objects.isNull(CONTEXT_HOLDER.get())) {
            return "";
        }
        return CONTEXT_HOLDER.get().getUserName();
    }

    public static void remove() {
        CONTEXT_HOLDER.remove();
    }

}
