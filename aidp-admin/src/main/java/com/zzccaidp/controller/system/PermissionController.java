package com.zzccaidp.controller.system;

import com.zzccaidp.service.system.PermissionService;
import com.zzccaidp.vo.PageRequest;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.ResHeader;
import com.zzccaidp.vo.system.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description: 权限
 * @Author: WB233500
 * @Createtime: 14:49
 * @Version: 1.0
 */
@RestController
@RequestMapping("/permission")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;


    @PostMapping("/list")
    public PageResponse<PermissionOut> list(@RequestBody PageRequest<PermissionIn> pageRequest) {
        return permissionService.list(pageRequest);
    }

    @PostMapping("/add")
    public ResHeader add(@RequestBody PermissionIn permissionIn) {
        ResHeader out = new ResHeader();
        out.setSuccessCode();
        permissionService.add(permissionIn);
        return out;
    }

    @PostMapping("/delete")
    public ResHeader delete(@RequestBody PermissionIn permissionIn) {
        ResHeader out = new ResHeader();
        out.setSuccessCode();
        permissionService.delete(permissionIn);
        return out;
    }

    @PostMapping("/update")
    public ResHeader update(@RequestBody PermissionIn permissionIn) {
        ResHeader out = new ResHeader();
        out.setSuccessCode();
        permissionService.update(permissionIn);
        return out;
    }

    @PostMapping("/tree")
    public PermissionTreeOut getPermissionTree(@RequestBody PermissionTreeIn permissionTreeIn) {
        PermissionTreeOut permissionTreeOut = new PermissionTreeOut();
        permissionTreeOut.setSuccessCode();
        permissionTreeOut.setPermissionTreeList(permissionService.getPermissionTree(permissionTreeIn));
        return permissionTreeOut;
    }

    @PostMapping("/listPermissionByRole")
    public PermissionIdListOut listPermissionByRole(@RequestBody PermissionTreeIn permissionTreeIn) {
        PermissionIdListOut out = new PermissionIdListOut();
        out.setPermissionIdList(permissionService.listPermissionByRole(permissionTreeIn));
        out.setSuccessCode();
        return out;
    }
}
