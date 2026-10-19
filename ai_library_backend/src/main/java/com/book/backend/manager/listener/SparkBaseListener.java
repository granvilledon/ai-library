package com.book.backend.manager.listener;

import com.book.backend.manager.exception.SparkException;
import com.book.backend.manager.model.SparkMessage;
import com.book.backend.manager.model.request.SparkRequest;
import com.book.backend.manager.model.response.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 讯飞星火WebSocket基础监听器
 */
public class SparkBaseListener extends WebSocketListener {
    private static final Logger logger = LoggerFactory.getLogger(SparkBaseListener.class);
    public ObjectMapper objectMapper;
    private SparkRequest sparkRequest;

    public SparkBaseListener() {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 收到文本消息时调用(需子类重写)
     *
     * @param content       回答内容
     * @param usage         Token消耗统计
     * @param status        会话状态(0-首次结果, 1-中间结果, 2-最后结果)
     * @param sparkRequest  请求参数
     * @param sparkResponse 响应数据
     * @param webSocket     WebSocket连接
     */
    public void onMessage(String content, SparkResponseUsage usage, Integer status, SparkRequest sparkRequest, SparkResponse sparkResponse, WebSocket webSocket) {
    }

    /**
     * 收到Function调用时调用(需子类重写)
     *
     * @param functionCall  Function调用对象
     * @param usage         Token消耗统计
     * @param status        会话状态(0-首次结果, 1-中间结果, 2-最后结果)
     * @param sparkRequest  请求参数
     * @param sparkResponse 响应数据
     * @param webSocket     WebSocket连接
     */
    public void onFunctionCall(SparkResponseFunctionCall functionCall, SparkResponseUsage usage, Integer status, SparkRequest sparkRequest, SparkResponse sparkResponse, WebSocket webSocket) {
    }

    @Override
    public final void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
        String requestJson = null;
        try {
            requestJson = objectMapper.writeValueAsString(sparkRequest);
        } catch (JsonProcessingException e) {
            throw new SparkException(400, "请求数据 SparkRequest 序列化失败", e);
        }
        webSocket.send(requestJson);
    }

    @Override
    public final void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        SparkResponse sparkResponse;

        try {
            sparkResponse = objectMapper.readValue(text, SparkResponse.class);
        } catch (JsonProcessingException e) {
            webSocket.close(1000, "");
            throw new SparkException(500, "响应数据 SparkResponse 解析失败：" + text, e);
        }
        SparkResponseHeader header = sparkResponse.getHeader();
        if (null == header) {
            webSocket.close(1000, "");
            throw new SparkException(500, "响应数据不完整 SparkResponse.header为null，完整响应：" + text);
        }

        Integer code = header.getCode();
        if (0 != code) {
            webSocket.close(1000, "");
            throw SparkException.bizFailed(code);
        }

        SparkResponseChoices choices = sparkResponse.getPayload().getChoices();
        List<SparkMessage> messages = choices.getText();
        StringBuilder stringBuilder = new StringBuilder();
        SparkResponseFunctionCall functionCall = null;

        SparkResponseUsage usage = sparkResponse.getPayload().getUsage();
        Integer status = header.getStatus();

        for (SparkMessage message : messages) {
            if (message.getFunction_call() != null) {
                functionCall = message.getFunction_call();
                break;
            }
            stringBuilder.append(message.getContent());
        }
        if (functionCall != null) {
            this.onFunctionCall(functionCall, usage, status, sparkRequest, sparkResponse, webSocket);
        } else {
            String content = stringBuilder.toString();
            this.onMessage(content, usage, status, sparkRequest, sparkResponse, webSocket);
        }

        if (2 == status) {
            webSocket.close(1000, "");
        }
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, Response response) {
        logger.error("讯飞星火api发生异常：", t);
    }

    public SparkRequest getSparkRequest() {
        return sparkRequest;
    }

    public void setSparkRequest(SparkRequest sparkRequest) {
        this.sparkRequest = sparkRequest;
    }
}
