package com.zzccaidp.service.system;

import com.zzccaidp.common.AsyncThreadPoolUtil;
import com.zzccaidp.common.RedisUtil;
import com.zzccaidp.common.SnowflakeUtil;
import com.zzccaidp.dao.system.SystemParamsDO;
import com.zzccaidp.exception.BusinessException;
import com.zzccaidp.mapper.system.SystemParamsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.zzccaidp.common.RedisKeyCommon.zzccAIDP_SYSTEM_PARAMS;
import static com.zzccaidp.enums.ErrCodeEnum.M0006;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 10:29
 * @Version: 1.0
 */
@Service
@Slf4j
public class SystemParamsService {

    @Autowired
    private SystemParamsMapper systemParamsMapper;

    @Autowired
    private RedisUtil redisUtil;

    public void updateSysparams(SystemParamsDO sysParamsDO) {
        systemParamsMapper.updateByPrimaryKeySelective(sysParamsDO);
        AsyncThreadPoolUtil.getThreadPoolExecutor().execute(this::refresh);
    }


    public void deleteSysparam(String id) {
        SystemParamsDO sysParamsDO = systemParamsMapper.selectByParamKey(id);
        if (sysParamsDO == null) {
            log.error("系统参数不存在");
        }
        systemParamsMapper.deleteByPrimaryKey(id);
        AsyncThreadPoolUtil.getThreadPoolExecutor().execute(this::refresh);
    }

    public void addSysparams(SystemParamsDO sysParamsDO) {
        SystemParamsDO paramsDO = new SystemParamsDO();
        paramsDO.setParamsKey(sysParamsDO.getParamsKey());
        if (!systemParamsMapper.select(paramsDO).isEmpty()) {
            throw new BusinessException(M0006);
        }
        sysParamsDO.setId(SnowflakeUtil.nextIdStr());
        systemParamsMapper.insert(sysParamsDO);
        AsyncThreadPoolUtil.getThreadPoolExecutor().execute(this::refresh);
    }

    public void updateSysParamsList(List<SystemParamsDO> sysParamsDOList) {
        for (SystemParamsDO sysParamsDO : sysParamsDOList) {
            if (Objects.isNull(sysParamsDO.getId())) {
                sysParamsDO.setId(SnowflakeUtil.nextIdStr());
                systemParamsMapper.insert(sysParamsDO);
            } else {
                systemParamsMapper.updateByPrimaryKeySelective(sysParamsDO);
            }
        }
        AsyncThreadPoolUtil.getThreadPoolExecutor().execute(this::refresh);
    }

    public List<SystemParamsDO> listSysParams(SystemParamsDO sysParamsDO) {
        return systemParamsMapper.listSysParamsByDO(sysParamsDO);
    }

    public void refresh() {
        List<SystemParamsDO> sysParamsDOList = systemParamsMapper.selectAll();
        Map<String, String> map = new HashMap<>();
        for (SystemParamsDO systemParamsDO : sysParamsDOList) {
            map.put(systemParamsDO.getParamsKey(), systemParamsDO.getParamsValue());
        }
        redisUtil.putKvByMap(zzccAIDP_SYSTEM_PARAMS, map);
        log.info("----刷新参数缓存[{}]", sysParamsDOList);
    }

    public SystemParamsDO getSysParam(String paramKey){
        return systemParamsMapper.selectByParamKey(paramKey);
    }

    public String getSysParamValue(String paramKey, String defaultValue){
        SystemParamsDO sysParam = this.getSysParam(paramKey);
        if (sysParam == null) {
            return defaultValue;
        }
        return sysParam.getParamsValue();
    }
}
