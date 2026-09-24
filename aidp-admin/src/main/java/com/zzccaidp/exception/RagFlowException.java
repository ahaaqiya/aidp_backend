package com.zzccaidp.exception;

import com.zzccaidp.enums.ErrCodeEnum;

/**
 * @author zhangtiantian
 * @date 2026/3/25
 */
public class RagFlowException extends BusinessException {

    private static final long serialVersionUID = 5936206454011379656L;

    public RagFlowException(ErrCodeEnum err){
        super(err);
    }

    public RagFlowException(ErrCodeEnum err, String errMsg){
        super(err, errMsg);
    }
}
