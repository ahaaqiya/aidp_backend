package com.zzccaidp.config;

import tk.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Mapper 扫描配置（替代 bades-db 的 mapper 扫描）
 * 显式注册 tk.mybatis 的 MapperScannerConfigurer，扫描通用 Mapper 接口
 */
@Configuration
public class MyBatisMapperScanConfig {

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer configurer = new MapperScannerConfigurer();
        configurer.setBasePackage("com.zzccaidp.mapper");
        return configurer;
    }
}
