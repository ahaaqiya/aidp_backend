package com.zzccaidp.exception;


import com.zzccaidp.enums.ErrCodeEnum;

import java.io.Serializable;

/**
 * 参数校验异常
 *
 * @author zhangtiantian
 * @date 2025/3/24
 */
public class ValidException extends BusinessException implements Serializable {
    private static final long serialVersionUID = 819383660894696391L;

    public ValidException(ErrCodeEnum err) {
        super(err);
    }


    /**
     * 若异常信息需要组装等情况优先使用该构造器
     *
     * @param code   业务异常代码
     * @param params 业务异常信息
     */
    public ValidException(ErrCodeEnum code, Object... params) {
        super(code, String.format(code.getErrMsg(), params));
    }
}
