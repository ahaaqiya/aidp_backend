package com.zzccaidp.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 丢弃任务策略
 * @author zhangtiantian
 * @date 2025/4/18
 */
public class AsyncDiscardPolicy implements RejectedExecutionHandler {

    private final String tpName;

    public AsyncDiscardPolicy(String tpName) {
        this.tpName = tpName;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncDiscardPolicy.class);

    @Override
    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
        LOGGER.warn("线程池{}任务丢弃，线程池已满:activeCount:{}, queueSize:{}, poolSize:{}",
                tpName,
                threadPoolExecutor.getActiveCount(),
                threadPoolExecutor.getQueue().size(),
                threadPoolExecutor.getPoolSize());
    }
}
