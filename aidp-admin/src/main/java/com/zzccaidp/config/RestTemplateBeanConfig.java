package com.zzccaidp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 11:02
 * @Version: 1.0
 */
@Configuration
public class RestTemplateBeanConfig {

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory clientHttpRequestFactory) {
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        // 设置默认的请求头
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("User-Agent", "MyForwardService/1.0");
            return execution.execute(request, body);
        });
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // 设置连接超时时间（单位：毫秒）
        factory.setConnectTimeout(5000);
        // 设置读取超时时间（单位：毫秒）
        factory.setReadTimeout(5000);
        return factory;
    }
}
