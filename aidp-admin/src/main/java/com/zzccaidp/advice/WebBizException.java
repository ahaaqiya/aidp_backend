package com.zzccaidp.advice;

/**
 * 
 * @author OneThin
 *
 */
public class WebBizException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public WebBizException() {}

    public WebBizException(String msg) {
        super(msg);
    }

    public WebBizException(String msg, Throwable t) {
        super(msg, t);
    }

    public WebBizException(Throwable t) {
        super(t);
    }
}
