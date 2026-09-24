package com.zzccaidp.exception;

import com.zzccaidp.common.exception.BaseBizException;
import com.zzccaidp.common.exception.ErrCodeBaseEnum;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 18:31
 * @Version: 1.0
 */
public class BusinessException extends BaseBizException {
    public BusinessException(ErrCodeBaseEnum errCodeEnum) {
        super(errCodeEnum);
    }

    public BusinessException(ErrCodeBaseEnum errCodeEnum, Object[] params) {
        super(errCodeEnum, params);
    }

    public BusinessException(ErrCodeBaseEnum errCodeEnum, Throwable cause) {
        super(errCodeEnum, cause);
    }

    public BusinessException(ErrCodeBaseEnum errCodeEnum, Object[] params, Throwable cause) {
        super(errCodeEnum, params, cause);
    }

    public BusinessException(ErrCodeBaseEnum errCodeEnum, String errMsg) {
        super(errCodeEnum, errMsg);
    }

    public BusinessException(ErrCodeBaseEnum errCodeEnum, String errMsg, Object[] params) {
        super(errCodeEnum, errMsg, params);
    }

    public BusinessException(ErrCodeBaseEnum errCodeEnum, String errMsg, Throwable cause) {
        super(errCodeEnum, errMsg, cause);
    }

    public BusinessException(ErrCodeBaseEnum errCodeEnum, String errMsg, Object[] params, Throwable cause) {
        super(errCodeEnum, errMsg, params, cause);
    }
}
