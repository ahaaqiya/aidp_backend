package com.zzccaidp.advice;

import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.exception.BusinessException;
import com.zzccaidp.vo.ResHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

/**
 * Controller异常
 *
 * @author bades
 */
@Slf4j
@ControllerAdvice
@ResponseBody
public class BaseExceptionHandlerAdvice {
    /**
     * 统一异常处理
     *
     * @param request
     * @param exception
     */
    @ExceptionHandler(Exception.class)
    public ResHeader exceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        log.error("Controller异常", exception);
        ResHeader out = new ResHeader();
        String msg;
        if (exception instanceof ConstraintViolationException) {
            Set<ConstraintViolation<?>> violations = ((ConstraintViolationException) exception).getConstraintViolations();
            StringBuilder resultMsg = new StringBuilder();
            for (ConstraintViolation<?> constraintViolation : violations) {
                String errorMsg = constraintViolation.getMessage();
                String property = constraintViolation.getPropertyPath().toString();
                resultMsg.append(property).append(":").append(errorMsg).append(";");
            }
            log.error("接口参数校验异常, 校验信息:{}, 异常信息:{}", resultMsg.toString(), exception);
            out.setResultcode(ErrCodeEnum.M0005.getErrCode());
            out.setResultmsg(resultMsg.toString());
        } else if (exception instanceof BusinessException) {
            out.setResultcode(((BusinessException) exception).getErrCode());
            out.setResultmsg(((BusinessException) exception).getErrMsg());
        } else {
            log.error("Controller异常", exception);
            out.setResultcode(ErrCodeEnum.ERROR.getErrCode());
            out.setResultmsg(ErrCodeEnum.ERROR.getErrMsg());
        }
        response.setContentType("application/json;charset=UTF-8");
        return out;
    }

    /**
     * 统一日期处理
     *
     * @param binder
     * @param
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), false));
    }

}
