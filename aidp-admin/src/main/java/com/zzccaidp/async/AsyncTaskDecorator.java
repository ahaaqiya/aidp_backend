package com.zzccaidp.async;

import com.zzccaidp.util.CollectUtil;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * @author zhangtiantian
 * @date 2025/4/18
 */
public class AsyncTaskDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        // MDC上下文
        Map<String, String> mdcContextMap = MDC.getCopyOfContextMap();
        // rpc上下文
        Map<String, Object> rpcContextMap = RpcContext.getContext().getObjectAttachments();

        return () -> {
            // 跨线程传递MDC上下文
            if (CollectUtil.isNotEmpty4map(mdcContextMap)) {
                MDC.setContextMap(mdcContextMap);
            }

            try {
                runnable.run();
            } finally {
                // 清理
                MDC.clear();;
            }
        };
    }
}
