package com.book.backend.manager.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;


/**
 * 文本Token使用统计 ($.payload.usage.text)
 */
public class SparkTextUsage implements Serializable {
    private static final long serialVersionUID = 8295933047877077971L;

    /**
     * 提问Token数(包含历史问题)
     */
    @JsonProperty("prompt_tokens")
    private Integer promptTokens;

    /**
     * 回答Token数
     */
    @JsonProperty("completion_tokens")
    private Integer completionTokens;

    /**
     * 总Token数(prompt_tokens + completion_tokens)，也是计费Token数
     */
    @JsonProperty("total_tokens")
    private Integer totalTokens;

    /**
     * 保留字段，可忽略
     */
    @JsonProperty("question_tokens")
    private Integer questionTokens;

    public Integer getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(Integer promptTokens) {
        this.promptTokens = promptTokens;
    }

    public Integer getCompletionTokens() {
        return completionTokens;
    }

    public void setCompletionTokens(Integer completionTokens) {
        this.completionTokens = completionTokens;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(Integer totalTokens) {
        this.totalTokens = totalTokens;
    }

    public Integer getQuestionTokens() {
        return questionTokens;
    }

    public void setQuestionTokens(Integer questionTokens) {
        this.questionTokens = questionTokens;
    }
}
