package com.zzccaidp.controller.system;

import cn.hutool.core.bean.BeanUtil;
import com.zzccaidp.dao.datasource.TableConfigDO;
import com.zzccaidp.dao.system.SystemParamsDO;
import com.zzccaidp.service.system.SystemParamsService;
import com.zzccaidp.vo.PageRequest;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.ResHeader;
import com.zzccaidp.vo.system.SystemParamsIn;
import com.zzccaidp.vo.system.SystemParamsOut;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 10:37
 * @Version: 1.0
 */
@RestController
@Slf4j
public class SystemParamsController {
    @Autowired
    private SystemParamsService systemParamsService;

    @PostMapping("/sysparams/updateList")
    public ResHeader updateSysParamsList(@RequestBody List<SystemParamsIn> systemParamsInList) {
        ResHeader res = new ResHeader();
        systemParamsService.updateSysParamsList(BeanUtil.copyToList(systemParamsInList, SystemParamsDO.class));
        res.setSuccessCode();
        return res;
    }


    @PostMapping("/sysparams/update")
    public ResHeader updateSysParams(@RequestBody SystemParamsIn systemParamsIn) {
        ResHeader res = new ResHeader();
        systemParamsService.updateSysparams(BeanUtil.copyProperties(systemParamsIn, SystemParamsDO.class));
        res.setSuccessCode();
        return res;
    }

    @PostMapping("/sysparams/create")
    public ResHeader createSysParam(@RequestBody SystemParamsIn systemParamsIn) {
        ResHeader res = new ResHeader();
        systemParamsService.addSysparams(BeanUtil.copyProperties(systemParamsIn, SystemParamsDO.class));
        res.setSuccessCode();
        return res;
    }

    @PostMapping("/sysparams/delete/{paramId}")
    public ResHeader deleteParam(@PathVariable String paramId) {
        ResHeader res = new ResHeader();
        systemParamsService.deleteSysparam(paramId);
        res.setSuccessCode();
        return res;
    }


    @PostMapping("/sysparams/list")
    public PageResponse<SystemParamsOut> listSysParams(@RequestBody PageRequest<SystemParamsIn> pageRequest) {
        PageResponse<SystemParamsOut> page = new PageResponse<>();
        PageHelper.startPage(pageRequest.getPageNum(), pageRequest.getPageSize());


        Page<TableConfigDO> tableConfigDOPage = PageHelper.startPage(pageRequest.getPageNum(), pageRequest.getPageSize())
                .doSelectPage(() -> systemParamsService.listSysParams(BeanUtil.copyProperties(pageRequest.getData(),
                        SystemParamsDO.class)));
        List<SystemParamsOut> systemParamsOutList =  BeanUtil.copyToList(tableConfigDOPage, SystemParamsOut.class);
        page.setTotalPage(tableConfigDOPage.getPages());
        page.setTotalCount(tableConfigDOPage.getTotal());
        page.setRecords(systemParamsOutList);
        page.setSuccessCode();
        return page;
    }

    @GetMapping("/sysparams/refresh")
    public ResHeader refreshSysParams() {
        ResHeader res = new ResHeader();
        systemParamsService.refresh();
        return res;
    }
}
