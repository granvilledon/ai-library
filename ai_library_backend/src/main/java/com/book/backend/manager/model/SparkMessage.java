package com.book.backend.manager.model;


import com.book.backend.manager.constant.SparkMessageRole;
import com.book.backend.manager.model.response.SparkResponseFunctionCall;

/**
 * 消息对象，用于封装对话内容
 */
public class SparkMessage {

    /**
     * 消息角色(user/assistant/system)
     */
    private String role;

    /**
     * 内容类型
     */
    private String content_type;

    /**
     * 函数调用对象
     */
    private SparkResponseFunctionCall function_call;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息索引(仅响应时有效)
     */
    private String index;

    public SparkMessage() {
    }

    public SparkMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    /**
     * 创建用户消息
     *
     * @param content 消息内容
     * @return SparkMessage实例
     */
    public static SparkMessage userContent(String content) {
        return new SparkMessage(SparkMessageRole.USER, content);
    }

    /**
     * 创建AI助手消息
     *
     * @param content 消息内容
     * @return SparkMessage实例
     */
    public static SparkMessage assistantContent(String content) {
        return new SparkMessage(SparkMessageRole.ASSISTANT, content);
    }

    /**
     * 创建系统指令消息
     *
     * @param content 指令内容
     * @return SparkMessage实例
     */
    public static SparkMessage systemContent(String content) {
        return new SparkMessage(SparkMessageRole.SYSTEM, content);
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getContent_type() {
        return content_type;
    }

    public void setContent_type(String content_type) {
        this.content_type = content_type;
    }

    public SparkResponseFunctionCall getFunction_call() {
        return function_call;
    }

    public void setFunction_call(SparkResponseFunctionCall function_call) {
        this.function_call = function_call;
    }
}
