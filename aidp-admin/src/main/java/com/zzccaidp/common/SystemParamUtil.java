package com.zzccaidp.common;

import com.zzccaidp.context.UserInfoContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.zzccaidp.common.RedisKeyCommon.zzccAIDP_SYSTEM_PARAMS;

/**
 * @Description: 从redis中读取系统参数的类
 * @Author: WB233500
 * @Createtime: 09:35
 * @Version: 1.0
 */
@Component
public class SystemParamUtil {
    @Autowired
    private RedisUtil redisUtil;

    public String getValue(String key) {
        return redisUtil.getMapValue(zzccAIDP_SYSTEM_PARAMS, key);
    }
}
