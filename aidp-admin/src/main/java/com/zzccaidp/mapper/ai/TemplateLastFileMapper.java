package com.zzccaidp.mapper.ai;

import com.zzccaidp.dao.ai.TemplateLastFileDO;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author zhangtiantian
 * @date 2026/5/7
 */
public interface TemplateLastFileMapper extends Mapper<TemplateLastFileDO> {

    void deleteByTemplateIdAndUserId(@Param("templateId") String templateId, @Param("userId") String userId);

    List<TemplateLastFileDO> selectByUserId(@Param("userId") String userId);
}
