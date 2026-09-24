package com.zzccaidp.mapper.ai;

import com.zzccaidp.dao.ai.AiFileResultDO;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 16:23
 * @Version: 1.0
 */
public interface AiFileResultMapper  extends Mapper<AiFileResultDO> {
    void insertAiFileResult(AiFileResultDO aiFileResultDO);

    void deleteByPrimaryKeys(@Param("fileIds") List<String> fileIds);

    List<AiFileResultDO> listAiFileHistoryResult(List<String> fileIds);
}
