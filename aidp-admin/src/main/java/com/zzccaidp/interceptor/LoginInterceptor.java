package com.zzccaidp.interceptor;

import com.zzccaidp.common.ConfigPropertieCommon;
import com.zzccaidp.common.RedisKeyCommon;
import com.zzccaidp.common.RedisUtil;
import com.zzccaidp.context.UserInfoContextHolder;
import com.zzccaidp.dao.system.UserDO;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.zzccaidp.common.ConfigPropertieCommon.SESSION_TIME;
import static com.zzccaidp.common.RedisKeyCommon.zzccAIDP_SYSTEM_PARAMS;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    private RedisUtil redisUtil;

    @Autowired
    public void setRedisCommonService(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI().replace("/zzccaidp", "");
        // 登录白名单
        String loginwhiteuri = redisUtil.getMapValue(RedisKeyCommon.zzccAIDP_SYSTEM_PARAMS, ConfigPropertieCommon.LOGIN_WHITE_URI);
        String[] whiteListUris = loginwhiteuri.split(",");
        AntPathMatcher pathMatcher = new AntPathMatcher();
        for (String whiteUri : whiteListUris) {
            if (pathMatcher.match(whiteUri, uri)) {
                return true;
            }
        }
        // 必输送token
        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)) {
            throw new BusinessException(ErrCodeEnum.M0401);
        }

        // 校验redis会话
        UserDO userInfo = redisUtil.getSessionByToken(token, Long.parseLong(redisUtil.getMapValue(RedisKeyCommon.zzccAIDP_SYSTEM_PARAMS, ConfigPropertieCommon.SESSION_TIME)));
        if (null == userInfo) {
            throw new BusinessException(ErrCodeEnum.M0401);
        }
        //回话校验通过后将用户名称放在线程上下文中。
        UserInfoContextHolder.addUserInfo(userInfo);
        return true;
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        UserInfoContextHolder.remove();
    }
}
