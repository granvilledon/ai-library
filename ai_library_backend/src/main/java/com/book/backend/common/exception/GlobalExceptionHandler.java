package com.book.backend.common.exception;

import com.book.backend.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理器
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理VueBookException异常
     *
     * @param e 异常对象
     * @return 统一返回结果
     */
    @ResponseBody
    @ExceptionHandler(VueBookException.class)
    @ResponseStatus(HttpStatus.OK)
    public R businessExceptionHandler(VueBookException e) {
        log.error("业务异常: {}", e.getErrMessage());
        return R.error(e.getErrMessage(), e.getCode());
    }

    /**
     * 处理参数验证异常
     *
     * @param e 异常对象
     * @return 统一返回结果
     */
    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数验证失败";
        log.error("参数验证异常: {}", message);
        return R.error(message, 400);
    }

    /**
     * 处理绑定异常
     *
     * @param e 异常对象
     * @return 统一返回结果
     */
    @ResponseBody
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R handleBindException(BindException e) {
        FieldError fieldError = e.getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        log.error("参数绑定异常: {}", message);
        return R.error(message, 400);
    }

    /**
     * 处理非法参数异常
     *
     * @param e 异常对象
     * @return 统一返回结果
     */
    @ResponseBody
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("非法参数异常: {}", e.getMessage());
        return R.error(e.getMessage(), 400);
    }

    /**
     * 处理空指针异常
     *
     * @param e 异常对象
     * @return 统一返回结果
     */
    @ResponseBody
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R handleNullPointerException(NullPointerException e) {
        log.error("空指针异常", e);
        return R.error("系统内部错误，请联系管理员", 500);
    }

    /**
     * 处理通用异常
     *
     * @param e 异常对象
     * @return 统一返回结果
     */
    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 - URL: {}, Exception: {}", request.getRequestURI(), e.getMessage(), e);
        return R.error("系统内部错误，请稍后重试", 500);
    }
}
