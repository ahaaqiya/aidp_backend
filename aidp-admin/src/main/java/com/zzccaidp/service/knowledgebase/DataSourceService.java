package com.zzccaidp.service.knowledgebase;


import com.alibaba.fastjson.JSONObject;
import com.zzccaidp.dao.knowledgebase.DataSourceDO;
import com.zzccaidp.mapper.knowledgebase.DataSourceMapper;
import com.zzccaidp.mapper.knowledgebase.DocumentMapper;
import com.zzccaidp.vo.knowledgebase.DataSourceIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


import java.util.Date;
import java.util.List;

@Service
public class DataSourceService {

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private DocumentMapper documentMapper;

    public List<DataSourceDO> findAll(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return dataSourceMapper.selectAll();
        }
        return dataSourceMapper.searchByKeyword(keyword);
    }

    public DataSourceDO findById(Long id) {
        return dataSourceMapper.selectById(id);
    }

    public DataSourceDO findByName(String name) {
        return dataSourceMapper.selectByName(name);
    }

    @Transactional
    public DataSourceDO create(DataSourceIn request) {
        DataSourceDO ds = new DataSourceDO();
        ds.setName(request.getName());
        ds.setChannel(request.getChannel());
        ds.setDescription(request.getDescription());
        ds.setDocumentCount(0);
        ds.setTypeDistribution("{}");
        ds.setConfig(request.getConfig());
        ds.setCreateTime(new Date());
        ds.setUpdateTime(new Date());
        dataSourceMapper.insert(ds);
        return ds;
    }

    @Transactional
    public DataSourceDO update(Long id, DataSourceIn request) {
        DataSourceDO ds = dataSourceMapper.selectById(id);
        if (ds == null) {
            return null;
        }
        if (request.getName() != null) {
            ds.setName(request.getName());
        }
        if (request.getChannel() != null) {
            ds.setChannel(request.getChannel());
        }
        if (request.getDescription() != null) {
            ds.setDescription(request.getDescription());
        }
        if (request.getConfig() != null) {
            ds.setConfig(request.getConfig());
        }
        ds.setUpdateTime(new Date());
        dataSourceMapper.update(ds);
        return ds;
    }

    @Transactional
    public boolean delete(Long id) {
        DataSourceDO ds = dataSourceMapper.selectById(id);
        if (ds == null) {
            return false;
        }
        dataSourceMapper.deleteById(id);
        return true;
    }

    @Transactional
    public DataSourceDO sync(Long id) {
        DataSourceDO ds = dataSourceMapper.selectById(id);
        if (ds == null) {
            return null;
        }
        ds.setLastSyncTime(new Date());
        dataSourceMapper.update(ds);
        return ds;
    }

    public List<DataSourceDO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return dataSourceMapper.selectAll();
        }
        return dataSourceMapper.searchByKeyword(keyword);
    }

    public boolean existsByName(String name) {
        return dataSourceMapper.existsByName(name);
    }

    @Transactional
    public DataSourceDO createRepo(java.util.Map<String, Object> config) {
        DataSourceDO ds = new DataSourceDO();
        ds.setName((String) config.get("name"));
        ds.setChannel((String) config.get("channel"));
        ds.setDescription((String) config.get("description"));
        ds.setDocumentCount(0);
        ds.setTypeDistribution("{}");
        ds.setConfig(config.get("config") != null ? config.get("config").toString() : null);
        ds.setCreateTime(new Date());
        ds.setUpdateTime(new Date());
        dataSourceMapper.insert(ds);
        return ds;
    }

    public String selectTypeDistribution(String channel) {
        List<JSONObject> jsonObjectList = documentMapper.selectTypeDistribution(channel);
        JSONObject jsonObject = new JSONObject();
        jsonObjectList.forEach(var -> {
            jsonObject.put(var.getString("doc_type"), var.getInteger("summarize"));
        });
        return jsonObject.toJSONString();
    }

    public Integer selectDocCountByDocType(String channel) {
        return documentMapper.selectDocCountByChannel(channel);
    }
}