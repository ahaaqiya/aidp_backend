package com.zzccaidp.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * @Description: WEBCLIENT配置类
 * @Author: WB233500
 * @Createtime: 10:21
 * @Version: 1.0
 */
@Configuration
public class WebCilentBeanConfig {

    @Value("${ragFlow.endpoint:http://203.10.236.3}")
    private String ragFlowEndpoint;


    @Bean("ansycWebClient")
    public WebClient ansycWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .responseTimeout(Duration.ofMillis(600000));

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(20971520))
                .build();
        WebClient webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();
        return webClient;
    }

    @Bean("ragFlowWebClient")
    public WebClient webClient() {
        return WebClient.builder().baseUrl(ragFlowEndpoint).build();
    }
}
