package com.zzccaidp.controller.system;

import com.zzccaidp.service.system.UserService;
import com.zzccaidp.vo.PageRequest;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.ResHeader;
import com.zzccaidp.vo.system.UserIn;
import com.zzccaidp.vo.system.UserOut;
import com.zzccaidp.vo.system.UserRoleIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 用户
 * @Author: WB233500
 * @Createtime: 14:48
 * @Version: 1.0
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/list")
    public PageResponse<UserOut> list(@RequestBody PageRequest<UserIn> pageRequest) {
        return userService.list(pageRequest);
    }

    @PostMapping("/add")
    public ResHeader add(@RequestBody UserIn userIn) {
        ResHeader out = new ResHeader();
        out.setSuccessCode();
        userService.add(userIn);
        return out;
    }

    @PostMapping("/delete")
    public ResHeader delete(@RequestBody UserIn userIn) {
        ResHeader out = new ResHeader();
        out.setSuccessCode();
        userService.delete(userIn);
        return out;
    }

    @PostMapping("/update")
    public ResHeader update(@RequestBody UserIn userIn) {
        ResHeader out = new ResHeader();
        out.setSuccessCode();
        userService.update(userIn);
        return out;
    }

    @PostMapping("/addRole")
    public ResHeader addRole(@RequestBody UserRoleIn userRoleIn){
        ResHeader out = new ResHeader();
        out.setSuccessCode();
        userService.addRole(userRoleIn);
        return out;
    }
}
