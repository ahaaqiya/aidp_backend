package com.zzccaidp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.MultipartConfigElement;

/**
 * 类功能：
 *
 * @author wangxy
 * @date 2020/9/1 16:14
 */
@Configuration
public class IndexSwaggerConfig {

    @Value("${maxFile.size}")
    private String maxFileSize;

    @Bean
    public MultipartConfigElement multipartConfigElement(){
        MultipartConfigFactory factory = new MultipartConfigFactory();
        // 设置单个附件上传大小
//        factory.setMaxFileSize(maxFileSize);
//        factory.setMaxRequestSize(maxFileSize);
        return factory.createMultipartConfig();
    }
}

