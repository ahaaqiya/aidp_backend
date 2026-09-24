package com.zzccaidp.mapper.datasource;

import com.zzccaidp.dao.datasource.TablePermissionDO;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author zhangtiantian
 * @date 2026/5/6
 */
public interface TablePermissionMapper extends Mapper<TablePermissionDO> {
    void batchInsert(@Param("dataList") List<TablePermissionDO> tablePermissionDOList);
}
