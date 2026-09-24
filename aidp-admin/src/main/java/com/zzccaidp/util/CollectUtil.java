package com.zzccaidp.util;

import com.zzccaidp.constants.BusinessConstant;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 集合工具
 * @author zhangtiantian
 * @date 2024/3/7
 */
public class CollectUtil extends CollectionUtils {

    /**
     * 分割数组
     * @param sourceList 源列表 @emptyAble
     * @param batchSize 分割大小  default:1
     * @return List<List<T>>
     */
    public static <T> List<List<T>> partition(List<T> sourceList, int batchSize) {
        if (isEmpty(sourceList)) {
            return new ArrayList<>();
        }
        if (batchSize == 0) {
            batchSize = 1;
        }
        return Lists.partition(sourceList, batchSize);
    }


    /**
     * 判断map是否为空
     * @param map Map<?, ?>
     * @return boolean
     */
    public static boolean isEmpty4Map(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     *判断map是否不为空
     * @param map Map<?, ?>
     * @return boolean
     */
    public static boolean isNotEmpty4map(Map<?, ?> map) {
        return !isEmpty4Map(map);
    }


    /**
     *判断列表是否有重复值  Object类必须重写hashCode和equals方法
     * @param list  列表
     * @return boolean
     */
    public static <T> boolean isHaveDuplicateValues(List<T> list) {
        if (isEmpty(list)) {
            return false;
        }
        return list.stream().distinct().count() != list.size();
    }

    /**
     * nullList兼容获取
     * @param list list
     * @return List<T>
     */
    public static <T> List<T> ifEmptyDefault(List<T> list) {
        return ifEmptyDefault(list, Lists.newArrayList());
    }

    /**
     * nullList兼容获取
     * @param list list
     * @param defaultList 空时的默认值 @notNull
     * @return List<T>
     */
    public static <T> List<T> ifEmptyDefault(List<T> list, List<T> defaultList) {
        return isEmpty(list) ? defaultList : list;
    }

    /**
     * nullMap兼容获取
     * @param map map
     * @return Map<K, V>
     */
    public static <K, V> Map<K, V> ifEmptyDefault(Map<K, V> map) {
        return ifEmptyDefault(map, new HashMap<>());
    }

    /**
     * nullMap兼容获取
     * @param map map
     * @param defaultMap 空时的默认值 @notNull
     * @return Map<K, V>
     */
    public static <K, V> Map<K, V> ifEmptyDefault(Map<K, V> map, Map<K, V> defaultMap) {
        return isEmpty4Map(map) ? defaultMap : map;
    }

    /**
     * 对象比对  用户更新操作，对比出新增、删除、更新的数据（需要有唯一主键）Object类必须重写hashCode和equals方法
     * @param dbDataList 数据库值 @notNull
     * @param newDataList 新值 @notNull
     * @param getPKFunction 获取主键方法  @notNull
     * @return  Map<String, List<T>> key：delete/insert/update
     */
    public static <T, PK> Map<String, List<T>> diffList4Update(Collection<T> newDataList, Collection<T> dbDataList, Function<T, PK> getPKFunction) {
        Map<String, List<T>> diffResult = new HashMap<>(3);
        if (null == newDataList || null == dbDataList || null == getPKFunction) {
            return diffResult;
        }
        Map<PK, T> pkReSelfMap = dbDataList.stream().collect(Collectors.toMap(getPKFunction, Function.identity()));

        // 更新
        diffResult.put(BusinessConstant.UPDATE_KEY, newDataList.stream().filter(nItem -> pkReSelfMap.containsKey(getPKFunction.apply(nItem))
                && !nItem.equals(pkReSelfMap.get(getPKFunction.apply(nItem)))).collect(Collectors.toList()));

        // 新增
        diffResult.put(BusinessConstant.INSERT_KEY, newDataList.stream().filter(nItem -> !pkReSelfMap.containsKey(getPKFunction.apply(nItem))).collect(Collectors.toList()));

        // 删除
        diffResult.put(BusinessConstant.DELETE_KEY, dbDataList.stream().filter(dItem -> newDataList.stream()
                .noneMatch(nItem -> getPKFunction.apply(dItem).equals(getPKFunction.apply(nItem)))).collect(Collectors.toList()));
        return diffResult;
    }

    /**
     * 对比对象是否全等（无主键比较）
     * 对象比对 返回多出的和少的 Object类必须重写hashCode和equals方法
     * @param source 源Collection @notNull
     * @param target 目标Collection @notNull
     * @return  Map<String, List<T>> key：delete/insert
     */
    public static Map<String, List<String>> diffList(Collection<String> source, Collection<String> target) {
        Map<String, List<String>> diffResult = new HashMap<>(2);
        if (source == null || target == null) {
            return diffResult;
        }
        diffResult.put(BusinessConstant.DELETE_KEY, source.stream().filter(item -> !target.contains(item)).collect(Collectors.toList()));
        diffResult.put(BusinessConstant.INSERT_KEY, target.stream().filter(item -> !source.contains(item)).collect(Collectors.toList()));
        return diffResult;
    }

    /**
     * 获取指定索引下值
     * @param collection 集合
     * @param index 索引
     */
    public static <T> T getElementByIndex(List<T> collection, int index) {
        if (isEmpty(collection) || index > collection.size() -1) {
            return null;
        }
        return collection.get(index);
    }

    /**
     * 去重
     * @param list list
     * @return Set<T>
     */
    public static <T> Set<T> toSet(List<T> list) {
        return new HashSet<>(ifEmptyDefault(list, Lists.newArrayList()));
    }

    /**
     * 去重
     * @param list list
     * @return Set<T>
     */
    public static <T> Set<T> toLinkedSet(List<T> list) {
        return new LinkedHashSet<>(ifEmptyDefault(list, Lists.newArrayList()));
    }

    /**
     * 去重
     * @param list list
     * @return List<T>
     */
    public static <T> List<T> deduplicate(List<T> list) {
        return new ArrayList<>(toLinkedSet(list));
    }
}
