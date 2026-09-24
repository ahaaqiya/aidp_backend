package com.zzccaidp.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 线程池配置
 */
@EnableAsync
@Configuration
public class AsyncTaskExecuteExecutor {

    @Bean("async-executor-taskExecute")
    public Executor AsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程数
        executor.setCorePoolSize(4);
        //线程池维护线程的最大数量，只有在缓冲队列满了之后才会申请超过核心线程数的线程
        executor.setMaxPoolSize(8);
        //缓存队列
        executor.setQueueCapacity(16);
        //空闲时间，当超过了核心线程数之外的线程在空闲时间到达之后会被销毁
        executor.setKeepAliveSeconds(200);
        //异步方法内部线程名称
        executor.setThreadNamePrefix("async-executor-taskExecute");
        //拒绝策略
        executor.setRejectedExecutionHandler(new AsyncDiscardPolicy("async-executor-taskExecute"));
        executor.initialize();
        return executor;
    }
}
