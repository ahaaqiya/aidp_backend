package com.zzccaidp.vo;

import com.zzccaidp.common.bean.BaseResponse;
import com.zzccaidp.enums.ErrCodeEnum;
import lombok.Data;

@Data
public class ResHeader extends BaseResponse {

    private String resultcode;

    private String resultmsg;

    public void setResResult(String resultcode, String resultmsg) {
        this.resultcode = resultcode;
        this.resultmsg = resultmsg;
    }

    public void setSuccessCode() {
        this.resultcode = ErrCodeEnum.SUCCESS.getErrCode();
        this.resultmsg = ErrCodeEnum.SUCCESS.getErrMsg();
    }

    public void setErrorCode() {
        this.resultcode = ErrCodeEnum.ERROR.getErrCode();
        this.resultmsg = ErrCodeEnum.ERROR.getErrMsg();
    }

    public void setSuccess() {
        setSuccessCode();
    }

    public void setError() {
        setErrorCode();
    }


}
