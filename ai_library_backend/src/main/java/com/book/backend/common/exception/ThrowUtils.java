package com.book.backend.common.exception;

/**
 * 异常抛出工具类
 */
public class ThrowUtils {

    /**
     * 条件成立时抛出指定异常
     *
     * @param condition        判断条件
     * @param runtimeException 要抛出的异常
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 条件成立时抛出业务异常
     *
     * @param condition 判断条件
     * @param errorCode 错误码枚举
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 条件成立时抛出带自定义消息的业务异常
     *
     * @param condition 判断条件
     * @param errorCode 错误码枚举
     * @param message   自定义错误消息
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }
}
