package com.book.backend.manager.model;


import com.book.backend.manager.constant.SparkApiVersion;
import com.book.backend.manager.model.request.*;
import com.book.backend.manager.model.request.function.SparkRequestFunctionMessage;
import com.book.backend.manager.model.request.function.SparkRequestFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 讯飞星火请求构建器
 */
public class SparkRequestBuilder {
    private static final Logger logger = LoggerFactory.getLogger(SparkRequestBuilder.class);

    private final SparkRequest sparkRequest;

    public SparkRequestBuilder() {
        sparkRequest = new SparkRequest();

        sparkRequest.setHeader(new SparkRequestHeader());
        sparkRequest.setParameter(new SparkRequestParameter(new SparkChatParameter()));
        sparkRequest.setPayload(new SparkRequestPayload(new SparkRequestMessage(new ArrayList<>())));
    }

    /**
     * 构建请求对象
     *
     * @return SparkRequest实例
     */
    public SparkRequest build() {
        SparkApiVersion apiVersion = sparkRequest.getApiVersion();
        if (sparkRequest.getPayload().getFunctions() != null && (apiVersion == SparkApiVersion.V2_0 || apiVersion == SparkApiVersion.V1_5)) {
            logger.warn("apiVersion is {}, this version does not support functions", apiVersion.getVersion());
        }
        sparkRequest.getParameter().getChat().setDomain(apiVersion.getDomain());
        return sparkRequest;
    }

    /**
     * 设置消息列表(必传)
     *
     * @param messages 消息列表，总Token数不能超过8192
     * @return SparkRequestBuilder实例
     */
    public SparkRequestBuilder messages(List<SparkMessage> messages) {
        sparkRequest.getPayload().getMessage().setText(messages);
        return this;
    }

    /**
     * 设置温度参数
     *
     * @param temperature 温度值(取值范围[0,1]，默认0.5)
     * @return SparkRequestBuilder实例
     */
    public SparkRequestBuilder temperature(Double temperature) {
        sparkRequest.getParameter().getChat().setTemperature(temperature);
        return this;
    }

    /**
     * 设置最大Token数
     *
     * @param maxTokens 最大Token数
     * @return SparkRequestBuilder实例
     */
    public SparkRequestBuilder maxTokens(Integer maxTokens) {
        sparkRequest.getParameter().getChat().setMaxTokens(maxTokens);
        return this;
    }

    /**
     * 设置Top-K采样参数
     *
     * @param topK Top-K值(取值范围[1,6]，默认4)
     * @return SparkRequestBuilder实例
     */
    public SparkRequestBuilder topK(Integer topK) {
        sparkRequest.getParameter().getChat().setTopK(topK);
        return this;
    }

    /**
     * 设置用户ID
     *
     * @param uid 用户ID(最大长度32)
     * @return SparkRequestBuilder实例
     */
    public SparkRequestBuilder uid(String uid) {
        sparkRequest.getHeader().setUid(uid);
        return this;
    }

    /**
     * 设置会话ID
     *
     * @param chatId 会话ID，需保证用户下唯一
     * @return SparkRequestBuilder实例
     */
    public SparkRequestBuilder chatId(String chatId) {
        sparkRequest.getParameter().getChat().setChatId(chatId);
        return this;
    }

    /**
     * 设置完整对话参数
     *
     * @param chatParameter 对话参数对象
     * @return SparkRequestBuilder实例
     */
    public SparkRequestBuilder chatParameter(SparkChatParameter chatParameter) {
        sparkRequest.getParameter().setChat(chatParameter);
        return this;
    }

    /**
     * 设置API版本
     *
     * @param apiVersion API版本枚举
     * @return SparkRequestBuilder实例
     */
    public SparkRequestBuilder apiVersion(SparkApiVersion apiVersion) {
        sparkRequest.setApiVersion(apiVersion);
        sparkRequest.getParameter().getChat().setDomain(apiVersion.getDomain());
        return this;
    }

    /**
     * 添加Function调用
     *
     * @param function Function消息对象
     * @return SparkRequestBuilder实例
     */
    public SparkRequestBuilder addFunction(SparkRequestFunctionMessage function) {
        SparkRequestFunctions functions = sparkRequest.getPayload().getFunctions();
        if (null == functions) {
            functions = new SparkRequestFunctions(new ArrayList<>());
            sparkRequest.getPayload().setFunctions(functions);
        }
        functions.getText().add(function);
        return this;
    }
}
