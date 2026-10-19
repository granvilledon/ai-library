package com.book.backend.common.exception;

/**
 * 自定义异常类
 */
public class VueBookException extends RuntimeException {

    private static final long serialVersionUID = 5565760508056698922L;

    /**
     * 错误消息
     */
    private String errMessage;

    /**
     * 错误码
     */
    private int code;

    public VueBookException() {
        super();
    }

    /**
     * 构造异常
     *
     * @param errMessage 错误消息
     */
    public VueBookException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    /**
     * 构造异常
     *
     * @param errorCode 错误码枚举
     * @param message   错误消息
     */
    public VueBookException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    /**
     * 构造异常
     *
     * @param errorCode 错误码枚举
     */
    public VueBookException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
    }

    /**
     * 抛出异常
     *
     * @param commonError 通用错误枚举
     */
    public static void cast(CommonError commonError) {
        throw new VueBookException(commonError.getErrMessage());
    }

    /**
     * 抛出异常
     *
     * @param errMessage 错误消息
     */
    public static void cast(String errMessage) {
        throw new VueBookException(errMessage);
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

}
