package com.zzccaidp.common.exception;

/**
 * 业务异常基类（本地实现，替代 com.bades.common.exception.BaseBizException）
 */
public class BaseBizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String errCode;

    private String errMsg;

    public BaseBizException(ErrCodeBaseEnum errCodeEnum) {
        super(errCodeEnum.getErrMsg());
        this.errCode = errCodeEnum.getErrCode();
        this.errMsg = errCodeEnum.getErrMsg();
    }

    public BaseBizException(ErrCodeBaseEnum errCodeEnum, Object[] params) {
        super(format(errCodeEnum.getErrMsg(), params));
        this.errCode = errCodeEnum.getErrCode();
        this.errMsg = format(errCodeEnum.getErrMsg(), params);
    }

    public BaseBizException(ErrCodeBaseEnum errCodeEnum, Throwable cause) {
        super(errCodeEnum.getErrMsg(), cause);
        this.errCode = errCodeEnum.getErrCode();
        this.errMsg = errCodeEnum.getErrMsg();
    }

    public BaseBizException(ErrCodeBaseEnum errCodeEnum, Object[] params, Throwable cause) {
        super(format(errCodeEnum.getErrMsg(), params), cause);
        this.errCode = errCodeEnum.getErrCode();
        this.errMsg = format(errCodeEnum.getErrMsg(), params);
    }

    public BaseBizException(ErrCodeBaseEnum errCodeEnum, String errMsg) {
        super(errMsg);
        this.errCode = errCodeEnum.getErrCode();
        this.errMsg = errMsg;
    }

    public BaseBizException(ErrCodeBaseEnum errCodeEnum, String errMsg, Object[] params) {
        super(format(errMsg, params));
        this.errCode = errCodeEnum.getErrCode();
        this.errMsg = format(errMsg, params);
    }

    public BaseBizException(ErrCodeBaseEnum errCodeEnum, String errMsg, Throwable cause) {
        super(errMsg, cause);
        this.errCode = errCodeEnum.getErrCode();
        this.errMsg = errMsg;
    }

    public BaseBizException(ErrCodeBaseEnum errCodeEnum, String errMsg, Object[] params, Throwable cause) {
        super(format(errMsg, params), cause);
        this.errCode = errCodeEnum.getErrCode();
        this.errMsg = format(errMsg, params);
    }

    private static String format(String msg, Object[] params) {
        if (msg == null || params == null || params.length == 0) {
            return msg;
        }
        String result = msg;
        for (Object param : params) {
            result = result.replaceFirst("\\{}", param == null ? "null" : param.toString());
        }
        return result;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
