package com.zzccaidp.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * springboot 跳转页面请求链接未到Controller层时就报错场景
 *
 * @author bades
 */
@Slf4j
@Component
public class BaseErrorViewResolver implements ErrorViewResolver {

	@Override
	public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
		log.error("统一错误处理 uri[{}], status[{}], model[{}]", request.getRequestURI(), status, model);

		if (status.is4xxClientError()) {
			return new ModelAndView("message404", model);
		} else {
			log.error("Http错误{}", status);
			return new ModelAndView("messageError", model);
		}
	}

}
