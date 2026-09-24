package com.zzccaidp.common;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author bades
 */
public class BeanUtils {

	private BeanUtils() {
	}

	public static <T> T copy(Object src, T target) {
		return copy(src, target, true);
	}

	public static <T> T copy(Object src, T target, boolean ignoreNull) {
		if (ignoreNull) {
			BeanUtil.copyProperties(src, target, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
		} else {
			BeanUtil.copyProperties(src, target);
		}
		return target;
	}

	public static Map<String, Object> beanToMap(Object object) {
		if (ObjectUtil.isEmpty(object)) {
			return Maps.newHashMap();
		}
		return JSON.parseObject(JSON.toJSONString(object), new TypeReference<Map<String, Object>>() {
		});
	}

}
