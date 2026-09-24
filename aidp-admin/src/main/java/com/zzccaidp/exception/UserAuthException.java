package com.zzccaidp.exception;


import com.zzccaidp.enums.ErrCodeEnum;


public class UserAuthException extends RuntimeException {

    private static final long serialVersionUID = -5987145301573177109L;

    private String errorCode;

    private String message;

    public UserAuthException(String errorCode){
        super(errorCode);
        this.errorCode=errorCode;
    }

    public UserAuthException(ErrCodeEnum err){
        super(err.getErrCode());
        this.errorCode=err.getErrCode();
        this.message=err.getErrMsg();
    }

    public UserAuthException(String errorCode, String message){
        super(errorCode);
        this.errorCode=errorCode;
        this.message=message;

    }

    public UserAuthException(String errorCode, String message, Throwable cause){
        super(errorCode,cause);
        this.errorCode=errorCode;
        this.message=message;
    }
}
