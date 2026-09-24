package com.zzccaidp.common;

import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.thread.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * @Description: 自定义线程池。后台异步任务的总线程池，禁止随意创建线程。防止在高并发流量下线程无限创建导致的内存问题
 * @Author: WB233500
 * @Createtime: 11:08
 * @Version: 1.0
 */
public class AsyncThreadPoolUtil {

    private static ThreadPoolExecutor threadPoolExecutor;

    private AsyncThreadPoolUtil() {

    }

    public synchronized static void initAsyncThreadPoolCommon(int corePoolSize, int maxPoolSize) {
        if (threadPoolExecutor == null) {
            ThreadFactory threadFactory = ThreadFactoryBuilder.create()
                    .setNamePrefix("aidp-pool-")//线程池前缀
                    .setDaemon(false)//是否守护线程
                    .build();
            threadPoolExecutor = ExecutorBuilder.create()
                    .setCorePoolSize(corePoolSize)
                    .setMaxPoolSize(maxPoolSize)
                    .setKeepAliveTime(60L, TimeUnit.SECONDS)
                    .setWorkQueue(new LinkedBlockingQueue<>())
                    .setThreadFactory(threadFactory)
                    .setHandler(new ThreadPoolExecutor.CallerRunsPolicy())
                    .build();
        }
    }

    public static ThreadPoolExecutor getThreadPoolExecutor() {
        if (threadPoolExecutor == null) {
            initAsyncThreadPoolCommon(10, 10);
        }
        return threadPoolExecutor;
    }

    public static void shutdownExecutor() {
        if (threadPoolExecutor != null) {
            threadPoolExecutor.shutdown();
        }
    }

}
