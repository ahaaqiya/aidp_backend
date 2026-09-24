package com.zzccaidp.async;

import com.alibaba.csp.sentinel.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** 异步事务相关
 * @author zhangtiantian
 * @date 2025/4/16
 */
@Component
@Slf4j
public class TransactionAsyncExecutor {

    private final AsyncExecutor asyncExecutor;

    public TransactionAsyncExecutor(AsyncExecutor asyncExecutor) {
        this.asyncExecutor = asyncExecutor;
    }

    /**
     * 如果有异步执行必须在当前线程事务提交之后执行，可以调用此方法
     * @param runnableName runnableName
     * @param runnable 需要异步执行的方法, 确保runnable已加@Async注解，无注解则同步执行
     */
    public void runAfterCommit(String runnableName, Runnable runnable) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    log.info("{}-{}事务已提交，开始回调afterCommit-{}", runnableName);
                    runnable.run();
                }
            });
        } else {
            log.warn("{}-{}非事务环境, 直接执行方法-{}", runnableName);
            runnable.run();
        }
    }

    public void runAfterCommit(Runnable runnable) {
        this.runAfterCommit(StringUtil.EMPTY, runnable);
    }

    /**
     * 如果有异步执行必须在当前线程事务提交之后执行，可以调用此方法
     * @param runnable 需要异步执行的方法
     */
    public void runAfterCommitAsync(Runnable runnable) {
        runAfterCommit(() -> asyncExecutor.execute(runnable));
    }


    @Component
    public static class AsyncExecutor {
        /**
         * 注意：这里必须填 {@code @Bean} 的名称（AsyncTaskConfig#AsyncExecutor 上的 "fileProcessingExecutor"），
         * 而不是线程名前缀 "async-executor-zhglt"，否则会抛
         * NoSuchBeanDefinitionException: No matching Executor bean found for qualifier。
         */
        @Async("fileProcessingExecutor")
        public void execute(Runnable runnable) {
            runnable.run();
        }
    }
}
