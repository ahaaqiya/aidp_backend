package com.zzccaidp.mapper.ai;

import com.zzccaidp.dao.ai.SysFileInfoDO;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 16:02
 * @Version: 1.0
 */
public interface SysFileInfoMapper extends Mapper<SysFileInfoDO> {
    List<SysFileInfoDO> selectAiFileByUser(String aiFile, String userId);
    // 新增的批量查询方法
    List<SysFileInfoDO> selectByPrimaryKeys(@Param("fileIds") List<String> fileIds);

    void deleteByPrimaryKeys(@Param("fileIds") List<String> fileIds);

    void updateFileInfo(SysFileInfoDO sysFileInfoDO);

    List<String> selectScratchFileByUserName(String userName);
}
