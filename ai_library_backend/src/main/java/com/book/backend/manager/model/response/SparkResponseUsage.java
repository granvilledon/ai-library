package com.book.backend.manager.model.response;

import java.io.Serializable;

/**
 * Token使用统计对象 ($.payload.usage)
 */
public class SparkResponseUsage implements Serializable {
    private static final long serialVersionUID = 2181817132625461079L;

    private SparkTextUsage text;

    public SparkTextUsage getText() {
        return text;
    }

    public void setText(SparkTextUsage text) {
        this.text = text;
    }
}
