package com.eduvideo.base.exception;

import java.io.Serializable;

/**
 * @author zkp15
 * @version 1.0
 * @description 异常响应体
 * @date 2023/6/13 22:23
 */
public class RestErrorResponse implements Serializable {
    private String errMessage;

    public RestErrorResponse(String errMessage){
        this.errMessage= errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }
}
