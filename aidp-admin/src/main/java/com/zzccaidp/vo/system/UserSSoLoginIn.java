package com.zzccaidp.vo.system;

import lombok.Data;

/**
 * @Description: sso
 * @Author: WB233500
 * @Createtime: 10:16
 * @Version: 1.0
 */
@Data
public class UserSSoLoginIn {
    private String ssoAuth;

    private String ssoSign;

    private String ssoAuthSM;

    private String ssoSignSM;
}
