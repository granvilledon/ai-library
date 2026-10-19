package com.book.backend.manager.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * 请求头参数 ($.header)
 */
public class SparkRequestHeader implements Serializable {
    private static final long serialVersionUID = -1426143090218924505L;

    /**
     * 应用ID，从开放平台控制台获取
     */
    @JsonProperty("app_id")
    private String appId;

    /**
     * 用户ID，用于区分不同用户(最大长度32)
     */
    private String uid;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
