package com.zzccaidp.vo;

import java.io.Serializable;

public class BaseResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer retCode;
    private String retMsg;
    private T data;

    public BaseResponse() {}

    public BaseResponse(Integer retCode, String retMsg) {
        this.retCode = retCode;
        this.retMsg = retMsg;
    }

    public BaseResponse(Integer retCode, String retMsg, T data) {
        this.retCode = retCode;
        this.retMsg = retMsg;
        this.data = data;
    }

    @Deprecated
    public void setErrorResponse(String retMsg) {
        this.retCode = 1;
        this.retMsg = retMsg;
    }

    public static <T> BaseResponse<T> success() {
        return new BaseResponse<>(0, "操作成功");
    }

    public static <T> BaseResponse<T> success(String retMsg) {
        return new BaseResponse<>(0, retMsg);
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, "操作成功", data);
    }

    public static <T> BaseResponse<T> success(String retMsg, T data) {
        return new BaseResponse<>(0, retMsg, data);
    }

    public static <T> BaseResponse<T> failed() {
        return new BaseResponse<>(1, "操作失败");
    }

    public static <T> BaseResponse<T> failed(String retMsg) {
        return new BaseResponse<>(1, retMsg);
    }

    public static <T> BaseResponse<T> failed(int retCode, String retMsg) {
        return new BaseResponse<>(retCode, retMsg);
    }

    public Integer getRetCode() {
        return retCode;
    }

    public void setRetCode(Integer retCode) {
        this.retCode = retCode;
    }

    public String getRetMsg() {
        return retMsg;
    }

    public void setRetMsg(String retMsg) {
        this.retMsg = retMsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}