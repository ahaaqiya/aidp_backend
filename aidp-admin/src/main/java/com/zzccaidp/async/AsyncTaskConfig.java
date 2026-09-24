package com.zzccaidp.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 */
@EnableAsync
@Configuration
public class AsyncTaskConfig {

    @Bean("fileProcessingExecutor")
    public ThreadPoolTaskExecutor AsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程数
        executor.setCorePoolSize(10);
        //线程池维护线程的最大数量，只有在缓冲队列满了之后才会申请超过核心线程数的线程
        executor.setMaxPoolSize(50);
        //缓存队列
        executor.setQueueCapacity(20);
        //空闲时间，当超过了核心线程数之外的线程在空闲时间到达之后会被销毁
        executor.setKeepAliveSeconds(200);
        //异步方法内部线程名称
        executor.setThreadNamePrefix("async-executor-zhglt");
        //拒绝策略
        executor.setRejectedExecutionHandler(new AsyncDiscardPolicy("async-executor-zhglt"));
        executor.initialize();
        return executor;
    }

    /**
     * 文档向量化专用单线程池：HTTP 开放接口「先落库快速返回、后台逐条向量化」使用。
     * <p>
     * 单线程 + 大队列 = 全局严格串行（一次只向量化一篇），对 RagFlow 解析服务的瞬时压力最小；
     * 队列基本不可能打满，兜底拒绝策略用 CallerRuns（由提交线程执行）而非丢弃，
     * 保证已受理的向量化任务不因队列边界而丢失。
     */
    @Bean("ragVectorExecutor")
    public ThreadPoolTaskExecutor ragVectorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程数：单线程，保证「挨个进行」的串行语义
        executor.setCorePoolSize(1);
        //最大线程数：与核心一致，不允许扩容并行
        executor.setMaxPoolSize(1);
        //缓存队列：单次推送上限 100 条，多批次排队余量放大到 2000
        executor.setQueueCapacity(2000);
        //线程名称：便于日志排查向量化任务执行情况
        executor.setThreadNamePrefix("rag-vector-executor");
        //拒绝策略：队列满时由提交线程执行，不丢任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
