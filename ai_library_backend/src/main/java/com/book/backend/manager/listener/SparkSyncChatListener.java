package com.book.backend.manager.listener;


import com.book.backend.manager.model.SparkSyncChatResponse;
import com.book.backend.manager.model.request.SparkRequest;
import com.book.backend.manager.model.response.SparkResponse;
import com.book.backend.manager.model.response.SparkResponseFunctionCall;
import com.book.backend.manager.model.response.SparkResponseUsage;
import okhttp3.Response;
import okhttp3.WebSocket;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 同步聊天监听器，用于阻塞式获取AI响应
 */
public class SparkSyncChatListener extends SparkBaseListener {
    private static final Logger logger = LoggerFactory.getLogger(SparkSyncChatListener.class);

    private final StringBuilder stringBuilder = new StringBuilder();

    private final SparkSyncChatResponse sparkSyncChatResponse;

    public SparkSyncChatListener(SparkSyncChatResponse sparkSyncChatResponse) {
        this.sparkSyncChatResponse = sparkSyncChatResponse;
    }

    @Override
    public void onMessage(String content, SparkResponseUsage usage, Integer status, SparkRequest sparkRequest, SparkResponse sparkResponse, WebSocket webSocket) {
        stringBuilder.append(content);
        if (2 == status) {
            sparkSyncChatResponse.setContent(stringBuilder.toString());
            sparkSyncChatResponse.setTextUsage(usage.getText());
            sparkSyncChatResponse.setOk(true);
        }
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, Response response) {
        logger.error("讯飞星火api发生异常", t);
        sparkSyncChatResponse.setOk(true);
        sparkSyncChatResponse.setException(t);
    }

    @Override
    public void onFunctionCall(SparkResponseFunctionCall functionCall, SparkResponseUsage usage, Integer status, SparkRequest sparkRequest, SparkResponse sparkResponse, WebSocket webSocket) {
        if (2 == status) {
            sparkSyncChatResponse.setContent(stringBuilder.toString());
            sparkSyncChatResponse.setTextUsage(usage.getText());
            sparkSyncChatResponse.setFunctionCall(functionCall);
            sparkSyncChatResponse.setOk(true);
        }
    }
}
