package com.zzccaidp.service.knowledgebase;

import cn.hutool.core.bean.BeanUtil;
import com.zzccaidp.common.RagResourceDict;
import com.zzccaidp.common.SnowflakeUtil;
import com.zzccaidp.context.UserInfoContextHolder;
import com.zzccaidp.dao.knowledgebase.RagResourceDO;
import com.zzccaidp.mapper.knowledgebase.RagResourceMapper;
import com.zzccaidp.vo.PageRequest;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.knowledgebase.RagResourceIn;
import com.zzccaidp.vo.knowledgebase.RagResourceOut;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 09:14
 * @Version: 1.0
 */
@Service
public class RagResourceService {

    @Autowired
    private RagResourceMapper ragResourceMapper;

    public PageResponse<RagResourceOut> list(PageRequest<RagResourceIn> pageRequest) {
        PageResponse<RagResourceOut> page = new PageResponse<>();
        PageHelper.startPage(pageRequest.getPageNum(), pageRequest.getPageSize());
        List<RagResourceOut> ragResourceOutList = BeanUtil.copyToList(ragResourceMapper.select(
                        BeanUtil.copyProperties(pageRequest.getData(), RagResourceDO.class)),
                RagResourceOut.class
        );
        PageInfo<RagResourceOut> pageInfo = new PageInfo<>(ragResourceOutList);
        page.setTotalPage(pageInfo.getPages());
        page.setTotalCount(pageInfo.getTotal());
        page.setRecords(ragResourceOutList);
        page.setSuccessCode();
        return page;
    }

    public void add(RagResourceIn ragResourceIn) {
        RagResourceDO ragResourceDO = BeanUtil.copyProperties(ragResourceIn, RagResourceDO.class);
        ragResourceDO.setRagResourceId(SnowflakeUtil.nextIdStr());
        ragResourceDO.setGmtCreateUser(UserInfoContextHolder.getUserInfo());
        ragResourceDO.setGmtModifiedUser(UserInfoContextHolder.getUserInfo());
        ragResourceDO.setGmtCreate(LocalDateTime.now());
        ragResourceDO.setGmtModified(LocalDateTime.now());
        ragResourceDO.setSyncStatus(RagResourceDict.SYNC_STATUS.SYNC.getStatus());
        ragResourceMapper.insert(ragResourceDO);
    }

    public void delete(RagResourceIn ragResourceIn) {
        ragResourceMapper.deleteByPrimaryKey(ragResourceIn.getRagResourceId());
    }

    public void update(RagResourceIn ragResourceIn) {
        RagResourceDO ragResourceDO = BeanUtil.copyProperties(ragResourceIn, RagResourceDO.class);
        ragResourceDO.setGmtModifiedUser(UserInfoContextHolder.getUserInfo());
        ragResourceDO.setGmtModified(LocalDateTime.now());
        ragResourceMapper.updateByPrimaryKey(ragResourceDO);
    }
}
