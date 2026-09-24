package com.zzccaidp.controller.system;

import com.zzccaidp.service.system.RoleService;
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
 * @Description: 角色
 * @Author: WB233500
 * @Createtime: 14:48
 * @Version: 1.0
 */
@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @PostMapping("/list")
    public PageResponse<RoleOut> list(@RequestBody PageRequest<RoleIn> pageRequest) {
        return roleService.list(pageRequest);
    }

    @PostMapping("/add")
    public ResHeader add(@RequestBody RoleIn roleIn) {
        ResHeader out = new ResHeader();
        out.setSuccessCode();
        roleService.add(roleIn);
        return out;
    }

    @PostMapping("/delete")
    public ResHeader delete(@RequestBody RoleIn roleIn) {
        ResHeader out = new ResHeader();
        out.setSuccessCode();
        roleService.delete(roleIn);
        return out;
    }

    @PostMapping("/update")
    public ResHeader update(@RequestBody RoleIn roleIn) {
        ResHeader out = new ResHeader();
        out.setSuccessCode();
        roleService.update(roleIn);
        return out;
    }

    @PostMapping("/addPermission")
    public ResHeader addPermission(@RequestBody RolePermissionIn rolePermissionIn) {
        ResHeader out = new ResHeader();
        out.setSuccessCode();
        roleService.addPermission(rolePermissionIn);
        return out;
    }

    @PostMapping("/addUser")
    public ResHeader addRole(@RequestBody RoleUserIn roleUserIn) {
        ResHeader out = new ResHeader();
        out.setSuccessCode();
        roleService.addUser(roleUserIn);
        return out;
    }

    @PostMapping("/listUser")
    public RoleUserOut listUser(@RequestBody RoleIn roleIn) {
        RoleUserOut roleUserOut = new RoleUserOut();
        roleUserOut.setData(roleService.listUser(roleIn));
        roleUserOut.setSuccessCode();
        return roleUserOut;
    }

}
