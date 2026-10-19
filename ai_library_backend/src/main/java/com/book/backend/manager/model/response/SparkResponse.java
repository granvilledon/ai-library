package com.book.backend.manager.model.response;

import java.io.Serializable;

/**
 * 讯飞星火API响应对象
 */
public class SparkResponse implements Serializable {
    private static final long serialVersionUID = 886720558849587945L;

    private SparkResponseHeader header;

    private SparkResponsePayload payload;

    public SparkResponseHeader getHeader() {
        return header;
    }

    public void setHeader(SparkResponseHeader header) {
        this.header = header;
    }

    public SparkResponsePayload getPayload() {
        return payload;
    }

    public void setPayload(SparkResponsePayload payload) {
        this.payload = payload;
    }
}
