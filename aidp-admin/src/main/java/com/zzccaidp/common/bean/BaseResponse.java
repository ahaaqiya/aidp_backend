package com.zzccaidp.common.bean;

import java.io.Serializable;

/**
 * 响应基类（本地实现，替代 com.bades.common.bean.BaseResponse）
 */
public class BaseResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String errCode;

    private String errMsg;

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
