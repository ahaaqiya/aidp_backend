package com.zzccaidp.controller.system;

import com.zzccaidp.exception.BusinessException;
import com.zzccaidp.service.system.UserLoginService;
import com.zzccaidp.vo.ReqHeader;
import com.zzccaidp.vo.ResHeader;
import com.zzccaidp.vo.system.UserLoginIn;
import com.zzccaidp.vo.system.UserLoginOut;
import com.zzccaidp.vo.system.UserSSoLoginIn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @Description: 用户登录，包括sso登录和form表单登录
 * @Author: WB233500
 * @Createtime: 09:28
 * @Version: 1.0
 */
@Slf4j
@RequestMapping(value = "/user")
@RestController
public class UserLoginController {

    @Autowired
    private UserLoginService userLoginService;

    @PostMapping(value = "/login")
    public UserLoginOut login(@Valid @RequestBody UserLoginIn in) throws BusinessException {
        log.info("---用户界面登录，输入请求参数[{}]---", in.getUserName());
        return userLoginService.login(in);
    }

    @PostMapping(value = "/ssologin")
    public UserLoginOut sso(@Valid @RequestBody UserSSoLoginIn in) throws BusinessException {
        log.info("---用户免密登录，输入请求参数[{}]---", in);
        return userLoginService.ssologin(in);
    }

    @RequestMapping(value = "/getMenuByloginId")
    public UserLoginOut getMenuByloginId(@RequestParam("loginId") String loginId) throws BusinessException {
        return userLoginService.getMenuByloginId(loginId);
    }

    @PostMapping(value = "/loginout")
    public ResHeader loginOut(@Valid @ModelAttribute ReqHeader in) throws BusinessException {
        ResHeader out = new ResHeader();
        out.setSuccessCode();
        userLoginService.loginOut(in);
        return out;
    }
}
