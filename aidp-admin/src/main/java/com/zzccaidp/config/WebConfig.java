package com.zzccaidp.config;

import com.zzccaidp.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * webConfig
 * @author zhangtiantian
 * @date 2024/05/17
 * @since 202406
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    LoginInterceptor logonInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logonInterceptor)
                .addPathPatterns("/**")
                .order(1);
    }
}
