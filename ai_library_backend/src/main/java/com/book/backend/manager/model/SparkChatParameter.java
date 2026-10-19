package com.book.backend.manager.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * 对话参数配置 ($.parameter.chat)
 */
public class SparkChatParameter implements Serializable {
    private static final long serialVersionUID = -1815416415486571475L;

    /**
     * API领域标识(默认: generalv2)
     */
    private String domain = "generalv2";

    /**
     * 温度参数，控制结果随机性(取值范围[0,1]，默认0.5)
     */
    private Double temperature;

    /**
     * 最大Token数限制
     * - V1.5: [1,4096]，默认2048
     * - V2.0: [1,8192]，默认2048
     * - V3.0: [1,8192]，默认2048
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    /**
     * Top-K采样参数(取值范围[1,6]，默认4)
     */
    @JsonProperty("top_k")
    private Integer topK;

    /**
     * 会话ID，用于关联用户会话
     */
    @JsonProperty("chat_id")
    private String chatId;

    /**
     * 创建默认参数配置
     *
     * @return SparkChatParameter实例
     */
    public static SparkChatParameter defaultParameter() {
        return new SparkChatParameter();
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
}
