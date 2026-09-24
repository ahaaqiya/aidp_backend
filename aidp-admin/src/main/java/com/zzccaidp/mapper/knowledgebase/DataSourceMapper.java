package com.zzccaidp.mapper.knowledgebase;

import com.zzccaidp.dao.knowledgebase.DataSourceDO;
import com.zzccaidp.in.DocumentAddIn;
import tk.mybatis.mapper.common.Mapper;

import java.util.Date;
import java.util.List;

public interface DataSourceMapper extends Mapper<DataSourceDO> {
    DataSourceDO selectById(Long id);

    List<DataSourceDO> selectAll();

    DataSourceDO selectByName(String name);

    List<DataSourceDO> searchByKeyword(String keyword);

    int insert(DataSourceDO dataSource);

    int update(DataSourceDO dataSource);

    int deleteById(Long id);

    int countAll();

    boolean existsByName(String name);

    DataSourceDO selectByChannel(String channel);

    void updateUpdTimeByChannel(String channel, Date date);
}