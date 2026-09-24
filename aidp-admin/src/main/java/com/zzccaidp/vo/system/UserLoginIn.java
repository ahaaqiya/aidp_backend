package com.zzccaidp.vo.system;

import com.zzccaidp.vo.ReqHeader;
import lombok.Data;

import javax.validation.constraints.NotNull;


/**
 * @Description: 用户登录入参
 * @Author: WB233500
 * @Createtime: 09:31
 * @Version: 1.0
 */
@Data
public class UserLoginIn extends ReqHeader {

    @NotNull
    private String userName;
    @NotNull
    private String password;
}
