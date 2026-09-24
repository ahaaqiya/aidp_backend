package com.zzccaidp.ssoc;

/**
 * SSOC 统一认证服务接口（本地接口，替代 com.bades.ssoc.SsocService）
 */
public interface SsocService {

    /**
     * 根据 SSO 票据解出统一身份用户 ID
     *
     * @param ssoAuthSM 认证票据
     * @param ssoSignSM 签名票据
     * @return 用户 ID，解密失败返回 null
     */
    String getSsoUserId(String ssoAuthSM, String ssoSignSM);
}
