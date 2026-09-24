package com.zzccaidp.common;


import com.alibaba.fastjson.JSON;
import com.zzccaidp.dao.system.UserDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("redisUtil")
public class RedisUtil {

    public static final String zzccAIDP_SESSION_ = "zzccAIDP_SESSION_";

    @Autowired
    public StringRedisTemplate stringTemplate;

    /**
     * 根据key从Redis取出value(注意此处的数据结构是hash)
     *
     * @param key
     * @return
     * @throws
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getMapByKey(String key) {
        Map map = stringTemplate.opsForHash().entries(key);
        Map<String, String> strMap = null;
        if (map == null) {
            strMap = new HashMap<>();
        } else
            strMap = map;
        return strMap;
    }


    /**
     * 根据key上传Map
     *
     * @param key
     * @return
     * @throws
     */
    public void putKvByMap(String key, Map<String, String> map) {
        stringTemplate.opsForHash().putAll(key, map);

    }

    public String getMapValue(String mapName, String key) {
        HashOperations<String, String, String> operations = stringTemplate.opsForHash();

        if (operations.hasKey(mapName, key)) {
            return operations.get(mapName, key);
        }
        return null;
    }


    /**
     * 根据key、hahkey从redis里面删除value(注意此处的数据结构是hash)
     *
     * @param key
     * @return
     * @throws
     */
    public void deleteValueByKeyAndHashKey(String key, String hashKey) {
        stringTemplate.opsForHash().delete(key, hashKey);
    }


    /**
     * 根据key从Redis取出hash结构的kv数目(注意此处的数据结构是hash)
     *
     * @param key
     * @return
     * @throws
     */
    public long getHashKvNumByKey(String key) {
        Long size = stringTemplate.opsForHash().size(key);
        return size;
    }

    /**
     * 通过token设置redis会话
     */
    public UserDO getSessionByToken(String token, long timeout) {
        ValueOperations<String, String> tokenInfo = stringTemplate.opsForValue();
        String redisTk = zzccAIDP_SESSION_ + token;
        if (tokenInfo.size(redisTk) > 0) {
            Object o = tokenInfo.get(redisTk);
            if (o != null && (o instanceof String)) {
                stringTemplate.expire(redisTk, timeout, TimeUnit.MILLISECONDS);
                UserDO userInfo = JSON.parseObject((String) o, UserDO.class);
                return userInfo;
            }
        }
        return null;
    }

    /**
     * 添加redis会话
     */
    public void addSeesionByToken(String token, String value, long timeout) {
        String redisTk = zzccAIDP_SESSION_ + token;
        ValueOperations<String, String> appTokens = stringTemplate.opsForValue();
        appTokens.set(redisTk, value, timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 根据key删除value
     *
     * @param key
     * @return
     */
    public boolean clearValueByKey(String key) {
        return stringTemplate.delete(key);
    }

    /**
     * 获取value
     *
     * @param key
     * @return
     */
    public Map<String, Object> getVal(String key) {
        ValueOperations<String, String> tks = stringTemplate.opsForValue();
        Map<String, Object> data = new HashMap<>();
        if (stringTemplate.hasKey(key)) {
            String val = tks.get(key);
            long time = stringTemplate.getExpire(key);
            data.put("val", val);
            data.put("time", time);
        }

        return data;
    }

    public String getValStr(String key) {
        ValueOperations<String, String> tks = stringTemplate.opsForValue();
        return tks.get(key);
    }

}
