package com.book.backend.manager.model;


import com.book.backend.manager.model.response.SparkResponseFunctionCall;
import com.book.backend.manager.model.response.SparkTextUsage;

import java.io.Serializable;

/**
 * 同步聊天响应对象
 */
public class SparkSyncChatResponse implements Serializable {
    private static final long serialVersionUID = -6785055441385392782L;

    /**
     * AI回答内容
     */
    private String content;

    /**
     * Function调用对象
     */
    private SparkResponseFunctionCall functionCall;

    /**
     * Token使用统计
     */
    private SparkTextUsage textUsage;

    /**
     * 完成标志(内部使用)
     */
    private boolean ok = false;

    /**
     * 异常信息
     */
    private Throwable exception;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public SparkResponseFunctionCall getFunctionCall() {
        return functionCall;
    }

    public void setFunctionCall(SparkResponseFunctionCall functionCall) {
        this.functionCall = functionCall;
    }

    public SparkTextUsage getTextUsage() {
        return textUsage;
    }

    public void setTextUsage(SparkTextUsage textUsage) {
        this.textUsage = textUsage;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }
}
