package com.zzccaidp.mapper.datasource;

import com.zzccaidp.dao.datasource.TableConfigDO;
import com.zzccaidp.vo.datasource.ListTableParam;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author zhangtiantian
 * @date 2026/5/6
 */
public interface TableConfigMapper extends Mapper<TableConfigDO> {
    List<TableConfigDO> listTable(ListTableParam data);
}
