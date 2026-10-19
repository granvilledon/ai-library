package com.book.backend.manager;


import com.book.backend.manager.constant.SparkApiVersion;
import com.book.backend.manager.exception.SparkException;
import com.book.backend.manager.listener.SparkBaseListener;
import com.book.backend.manager.listener.SparkSyncChatListener;
import com.book.backend.manager.model.SparkSyncChatResponse;
import com.book.backend.manager.model.request.SparkRequest;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 讯飞星火客户端，提供同步和异步聊天功能
 */
public class SparkClient {

    public String appid;

    public String apiKey;

    public String apiSecret;

    public OkHttpClient client = new OkHttpClient.Builder().build();

    /**
     * 发起流式聊天请求
     *
     * @param sparkRequest 请求参数
     * @param listener     响应监听器
     */
    public void chatStream(SparkRequest sparkRequest, SparkBaseListener listener) {
        sparkRequest.getHeader().setAppId(appid);
        listener.setSparkRequest(sparkRequest);

        SparkApiVersion apiVersion = sparkRequest.getApiVersion();
        String apiUrl = apiVersion.getUrl();

        String authWsUrl = null;
        try {
            authWsUrl = getAuthUrl(apiUrl).replace("http://", "ws://").replace("https://", "wss://");
        } catch (Exception e) {
            throw new SparkException(500, "构建鉴权url失败", e);
        }
        Request request = new Request.Builder().url(authWsUrl).build();
        client.newWebSocket(request, listener);
    }

    /**
     * 发起同步聊天请求
     *
     * @param sparkRequest 请求参数
     * @return 聊天响应
     */
    public SparkSyncChatResponse chatSync(SparkRequest sparkRequest) {
        SparkSyncChatResponse chatResponse = new SparkSyncChatResponse();
        SparkSyncChatListener syncChatListener = new SparkSyncChatListener(chatResponse);
        this.chatStream(sparkRequest, syncChatListener);
        while (!chatResponse.isOk()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
        }
        Throwable exception = chatResponse.getException();
        if (exception != null) {
            if (!(exception instanceof SparkException)) {
                exception = new SparkException(500, exception.getMessage());
            }
            throw (SparkException) exception;
        }
        return chatResponse;
    }

    /**
     * 生成鉴权URL
     *
     * @param apiUrl API地址
     * @return 鉴权后的URL
     */
    public String getAuthUrl(String apiUrl) throws Exception {
        URL url = new URL(apiUrl);
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        String preStr = "host: " + url.getHost() + "\n" +
                "date: " + date + "\n" +
                "GET " + url.getPath() + " HTTP/1.1";
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);

        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        String sha = Base64.getEncoder().encodeToString(hexDigits);

        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
        HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse("https://" + url.getHost() + url.getPath())).newBuilder().//
                addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8))).//
                addQueryParameter("date", date).//
                addQueryParameter("host", url.getHost()).//
                build();

        return httpUrl.toString();
    }
}
