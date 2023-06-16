package com.eduvideo.base.exception;

/**
 * @author zkp15
 * @version 1.0
 * @description 自定义异常
 * @date 2023/6/13 22:20
 */
public class EduVideoException extends RuntimeException {
    private String errMessage;

    public EduVideoException() {
        super();
    }

    public EduVideoException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public static void cast(CommonError commonError) {
        throw new EduVideoException(commonError.getErrMessage());
    }

    public static void cast(String errMessage) {
        throw new EduVideoException(errMessage);
    }
}
