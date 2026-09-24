package com.zzccaidp.mapper.system;

import com.zzccaidp.dao.system.SystemParamsDO;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @Description: 系统参数管理
 * @Author: WB233500
 * @Createtime: 10:17
 * @Version: 1.0
 */
public interface SystemParamsMapper extends Mapper<SystemParamsDO> {
    /**
     * 分页查询
     *
     * @param SystemParamsDO
     * @return SystemParamsDO
     */
    List<SystemParamsDO> listSysParamsByDO(SystemParamsDO SystemParamsDO);

    /**
     * 根据paramKey获取参数
     *
     * @param paramKey 参数key
     * @return SystemParamsDO
     */
    SystemParamsDO selectByParamKey(@Param("paramKey") String paramKey);

}
