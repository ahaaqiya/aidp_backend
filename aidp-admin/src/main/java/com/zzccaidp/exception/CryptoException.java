package com.zzccaidp.exception;


import com.zzccaidp.enums.ErrCodeEnum;

/**
 * @author zhangtiantian
 * @date 2025/11/7
 */
public class CryptoException extends RuntimeException {
    private static final long serialVersionUID = -5987145302573177109L;

    private String errorCode;

    private String message;

    public CryptoException(String errorCode){
        super(errorCode);
        this.errorCode=errorCode;
    }

    public CryptoException(ErrCodeEnum err){
        super(err.getErrCode());
        this.errorCode=err.getErrCode();
        this.message=err.getErrMsg();
    }

    public CryptoException(String errorCode, String message){
        super(errorCode);
        this.errorCode=errorCode;
        this.message=message;

    }

    public CryptoException(String errorCode, String message, Throwable cause){
        super(errorCode,cause);
        this.errorCode=errorCode;
        this.message=message;
    }
}
