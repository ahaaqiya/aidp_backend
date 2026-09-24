package com.zzccaidp.mapper.datasource;

import com.zzccaidp.dao.datasource.DataSourceConfigDO;
import com.zzccaidp.vo.datasource.ListDatasourceParam;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author zhangtiantian
 * @date 2026/5/6
 */
public interface DataSourceConfigMapper extends Mapper<DataSourceConfigDO> {

    List<DataSourceConfigDO> listDatasource(ListDatasourceParam data);
}
