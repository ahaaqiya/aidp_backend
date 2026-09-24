package com.zzccaidp.ssoc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * SSOC 本地桩实现（内网统一认证不可用时使用）
 * <p>
 * 本地模式：跳过国密票据校验，直接返回配置的本地用户，使免密登录（ssologin）可用。
 * 也可通过 ssoAuthSM 参数显式指定登录哪个用户（如 {"userId":"zhangsan"}）。
 */
@Slf4j
@Service("ssocService")
public class LocalSsocServiceStub implements SsocService {

    @Value("${ssoc.stub.enabled:true}")
    private boolean stubEnabled;

    /** 桩模式默认放行的本地用户 */
    @Value("${ssoc.stub.user-id:admin}")
    private String stubUserId;

    @Override
    public String getSsoUserId(String ssoAuthSM, String ssoSignSM) {
        if (!stubEnabled) {
            log.warn("SSOC 桩未启用（ssoc.stub.enabled=false），SSO 登录将被拒绝");
            return null;
        }
        // 若调用方把 ssoAuthSM 直接传成了 userId，则按传入用户放行，便于本地切换账号调试
        if (ssoAuthSM != null && ssoAuthSM.matches("[a-zA-Z0-9_\\-\\u4e00-\\u9fa5]{1,20}")
                && !"undefined".equalsIgnoreCase(ssoAuthSM) && !"null".equalsIgnoreCase(ssoAuthSM)) {
            log.warn("SSOC 本地桩：按传入票据放行用户[{}]", ssoAuthSM);
            return ssoAuthSM;
        }
        log.warn("SSOC 本地桩：放行默认用户[{}]", stubUserId);
        return stubUserId;
    }
}
