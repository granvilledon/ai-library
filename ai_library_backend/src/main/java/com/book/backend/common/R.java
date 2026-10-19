package com.book.backend.common;

import lombok.Data;

import java.util.HashMap;

/**
 * 通用返回结果封装类
 *
 * @param <T> 数据类型
 */
@Data
public class R<T> {
    /**
     * 响应状态码
     * - 200: 请求成功
     * - 201: 创建成功
     * - 204: 删除成功
     * - 400: 请求参数错误
     * - 401: 未授权
     * - 403: 禁止访问
     * - 404: 资源不存在
     * - 422: 验证错误
     * - 500: 服务器内部错误
     */
    private Integer status;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 扩展数据
     */
    private HashMap<String, Object> map = new HashMap<>();

    /**
     * 成功响应
     *
     * @param object  数据对象
     * @param message 响应消息
     * @return 统一返回结果
     */
    public static <T> R<T> success(T object, String message) {
        R<T> r = new R<T>();
        r.data = object;
        r.status = 200;
        r.msg = message;
        return r;
    }

    /**
     * 失败响应
     *
     * @param msg 错误消息
     * @return 统一返回结果
     */
    public static <T> R<T> error(String msg) {
        R<T> r = new R<T>();
        r.msg = msg;
        r.status = 400;
        return r;
    }

    /**
     * 失败响应(带状态码)
     *
     * @param msg  错误消息
     * @param code 状态码
     * @return 统一返回结果
     */
    public static <T> R<T> error(String msg, int code) {
        R<T> r = new R<T>();
        r.msg = msg;
        r.status = code;
        return r;
    }

    /**
     * 添加扩展数据
     *
     * @param key   键
     * @param value 值
     * @return 当前对象
     */
    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

}
