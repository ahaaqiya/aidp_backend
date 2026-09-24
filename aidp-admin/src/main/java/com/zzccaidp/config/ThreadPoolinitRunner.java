package com.zzccaidp.config;

import com.zzccaidp.common.AsyncThreadPoolUtil;
import com.zzccaidp.service.system.SystemParamsService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

/**
 * @Description: 后台异步任务的总线程池，禁止随意创建线程。防止在高并发流量下线程无限创建导致的内存问题
 * @Author: WB233500
 * @Createtime: 10:59
 * @Version: 1.0
 */
@Component
public class ThreadPoolinitRunner implements ApplicationRunner, DisposableBean {
    @Autowired
    private SystemParamsService service;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //创建线程池
        AsyncThreadPoolUtil.getThreadPoolExecutor();

        //初始化系统参数
        service.refresh();
    }

    @Override
    public void destroy() throws Exception {
        ExecutorService executorService = AsyncThreadPoolUtil.getThreadPoolExecutor();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
