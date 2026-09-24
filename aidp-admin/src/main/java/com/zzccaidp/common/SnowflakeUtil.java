package com.zzccaidp.common;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Description: 雪花算法
 * @Author: WB233500
 * @Createtime: 15:28
 * @Version: 1.0
 */
@Slf4j
public class SnowflakeUtil {
    private static final Snowflake snowflake;

    static {
        long workerId = 1; // 默认值
        long datacenterId = 1; // 可从配置读取

        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            // 使用IP哈希值生成节点ID
            workerId = Math.abs(ip.hashCode()) % 32;
        } catch (Exception e) {
            log.warn("Snowflake workerId初始化失败，使用默认值: {}", workerId, e);
        }

        snowflake = IdUtil.getSnowflake(datacenterId, workerId);
    }

    private SnowflakeUtil() {
        throw new UnsupportedOperationException("工具类不可实例化");
    }

    public static String nextIdStr() {
        return snowflake.nextIdStr();
    }

    public static long nextId() {
        return snowflake.nextId();
    }
}
