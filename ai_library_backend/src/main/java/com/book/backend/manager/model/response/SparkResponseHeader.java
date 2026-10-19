package com.book.backend.manager.model.response;

import java.io.Serializable;

/**
 * 响应头对象 ($.header)
 */
public class SparkResponseHeader implements Serializable {
    private static final long serialVersionUID = -2828057068263022569L;

    /**
     * 错误码(0表示正常，非0表示出错)
     */
    private Integer code;

    /**
     * 会话状态(0-首次结果, 1-中间结果, 2-最后结果)
     */
    private Integer status;

    /**
     * 会话描述信息
     */
    private String message;

    /**
     * 会话唯一ID，用于查询服务端日志
     */
    private String sid;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }
}
