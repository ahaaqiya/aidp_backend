package com.zzccaidp.common.exception;

/**
 * 错误码基接口（本地实现，替代 com.bades.common.exception.ErrCodeBaseEnum）
 */
public interface ErrCodeBaseEnum {

    String getErrCode();

    String getErrMsg();
}
