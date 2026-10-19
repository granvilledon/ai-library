package com.book.backend.manager.constant;

/**
 * 讯飞星火API版本枚举
 */
public enum SparkApiVersion {

    /**
     * V1.5版本
     */
    V1_5("v1.1", "https://spark-api.xf-yun.com/v1.1/chat", "general"),

    /**
     * V2.0版本
     */
    V2_0("v2.1", "https://spark-api.xf-yun.com/v2.1/chat", "generalv2"),

    /**
     * V3.0版本
     */
    V3_0("v3.1", "https://spark-api.xf-yun.com/v3.1/chat", "generalv3"),
    ;

    private final String version;
    private final String url;
    private final String domain;

    SparkApiVersion(String version, String url, String domain) {
        this.version = version;
        this.url = url;
        this.domain = domain;
    }

    public String getVersion() {
        return version;
    }

    public String getUrl() {
        return url;
    }

    public String getDomain() {
        return domain;
    }
}
