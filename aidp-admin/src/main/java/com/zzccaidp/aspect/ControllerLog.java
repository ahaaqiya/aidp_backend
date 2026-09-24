package com.zzccaidp.aspect;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 11:11
 * @Version: 1.0
 */
@Aspect
@Slf4j
@Component
public class ControllerLog {


    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void printLogBeforeRequest(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String calssName = joinPoint.getSignature().getDeclaringType().getName();
        Object[] args = joinPoint.getArgs();

        StringBuilder logMessage = new StringBuilder();
        logMessage.append("---调用方法:[").append(calssName).append(".").append(methodName).append("]，参数: [");

        for (Object arg : args) {
            if (arg instanceof MultipartFile || arg instanceof MultipartFile[]) {
                continue;
            }
            if (arg instanceof HttpServletResponse) {
                continue;
            }
            logMessage.append(JSON.toJSONString(arg)).append(",");
        }

        logMessage.append("]");
        log.info(logMessage.toString());
    }
}
