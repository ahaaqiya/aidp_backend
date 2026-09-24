package com.zzccaidp.util;

import com.zzccaidp.constants.SymbolConstant;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 字符串工具类
 * @author zhangtiantian
 * @date 2025/3/21
 */
public class StringUtil extends StringUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringUtil.class);

    /**
     * Collection join 默认逗号分割
     * @param list 数组
     * @param delimiter 分隔符
     * @param <T> String、Int等基础类型，Object慎用
     * @return String
     */
    public static <T> String join(Collection<T> list, String delimiter) {
        if (CollectUtil.isEmpty(list)) {
            return EMPTY;
        }
        return list.stream().map(String::valueOf).collect(Collectors.joining(delimiter));
    }

    /**
     * Collection join 默认逗号分割
     * @param list 数组
     * @param <T> String、Int等基础类型，Object慎用
     * @return String
     */
    public static <T> String join(Collection<T> list) {
        return join(list, SymbolConstant.COMMA);
    }

    public static String trim(String str){
        if(Objects.isNull(str)){
            return "";
        }
        return str.trim();
    }

    /**
     * 字符串转数组
     * @param str 字符串
     * @return List<String>
     */
    public static List<String> toSplit(String str) {
        return toSplit(str, SymbolConstant.COMMA);
    }

    /**
     * 字符串转数组
     * @param str 字符串
     * @param delimiter 分隔符
     * @return List<String>
     */
    public static List<String> toSplit(String str, String delimiter) {
        if (isBlank(str)) {
            return Lists.newArrayList();
        }
        return Arrays.asList(split(str, delimiter));
    }

    /**
     * 字符串空默认
     * @param str 字符串
     * @param defaultStr 默认值 @notNull
     * @return String
     */
    public static String ifBlankDefault(String str, String defaultStr) {
        if (isBlank(str)) {
            return defaultStr;
        }
        return str;
    }

    /**
     * 字符串空默认""
     * @param str 字符串
     * @return String 默认""
     */
    public static String ifBlankDefault(String str) {
        if (isBlank(str)) {
            return ifBlankDefault(str, StringUtil.EMPTY);
        }
        return str;
    }

    /**
     * 字符串转int数组  id转换
     * @param str 字符串
     * @return List<Int>
     */
    public static List<Integer> toSplitIntArr(String str) {
        if (isBlank(str)) {
            return Lists.newArrayList();
        }
        try {
            return toSplit(str, SymbolConstant.COMMA).stream().map(Integer::valueOf).collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("字符串转整型数组失败", e);
            return Lists.newArrayList();
        }
    }
    public static List<String> stringToList(String str,String splic){
        String[] strs = str.split(splic);
        if(strs.length == 0){
            return new ArrayList<>();
        }
        return Arrays.asList(strs);
    }

}
