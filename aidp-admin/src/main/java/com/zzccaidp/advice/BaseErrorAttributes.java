package com.zzccaidp.advice;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * springboot ajax请求链接未到Controller层时就报错场景
 *
 * @author bades
 */
@Component
public class BaseErrorAttributes extends DefaultErrorAttributes {

	/**
	 * 这里可以自定义组装ajax请求报错
	 *
	 * @param webRequest
	 * @param options
	 * @return
	 */
	@Override
	public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
		Map<String, Object> errorAttributes = new LinkedHashMap<>();
		errorAttributes.put("success", Boolean.FALSE);
		errorAttributes.put("status", -1);
		errorAttributes.put("msg", "服务端异常");
		return errorAttributes;
	}
}
