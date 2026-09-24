package com.zzccaidp.common;

/**
 * 枚举基接口（本地实现，替代 com.bades.db.BaseEnum）
 *
 * @param <V> 值类型
 * @param <D> 描述类型
 */
public interface BaseEnum<V, D> {

    V getVal();

    D getDesc();
}
