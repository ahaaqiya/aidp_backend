package com.zzccaidp.service.system;

import cn.hutool.core.bean.BeanUtil;
import com.zzccaidp.dao.system.OrgDO;
import com.zzccaidp.mapper.system.OrgMapper;
import com.zzccaidp.vo.PageRequest;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.system.OrgIn;
import com.zzccaidp.vo.system.OrgOut;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 09:52
 * @Version: 1.0
 */
@Service
public class OrgService {
    @Autowired
    private OrgMapper orgMapper;

    public PageResponse<OrgOut> list(PageRequest<OrgIn> pageRequest) {
        PageResponse<OrgOut> page = new PageResponse<>();
        PageHelper.startPage(pageRequest.getPageNum(), pageRequest.getPageSize());
        List<OrgDO> orgDOList = orgMapper.select(BeanUtil.copyProperties(pageRequest.getData(), OrgDO.class));
        List<OrgOut> orgOutList = BeanUtil.copyToList(orgDOList, OrgOut.class);
        PageInfo<OrgDO> pageInfo = new PageInfo<>(orgDOList);
        page.setTotalPage(pageInfo.getPages());
        page.setTotalCount(pageInfo.getTotal());
        page.setRecords(orgOutList);
        page.setSuccessCode();
        return page;
    }

    public void add(OrgIn orgIn) {
        orgMapper.insert(BeanUtil.copyProperties(orgIn, OrgDO.class));
    }

    /*
    删除机构时需要校验机构下是否还有人员存在，是否还有子机构存在。如果存在人员或者子机构，不允许删除。
     */
    public void delete(OrgIn orgIn) {
        orgMapper.deleteByPrimaryKey(orgIn.getId());
    }

    public void update(OrgIn orgIn) {
        orgMapper.updateByPrimaryKey(BeanUtil.copyProperties(orgIn, OrgDO.class));
    }
}
