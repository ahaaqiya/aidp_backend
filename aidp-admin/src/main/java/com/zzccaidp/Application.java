package com.zzccaidp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * @author bades
 */
@SpringBootApplication
// 加载 Dubbo 消费端配置（appCtx-dubbo.xml），用于调用 zsk 个人知识库推送接口
@ImportResource(locations = {"classpath*:appCtx-dubbo.xml"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
