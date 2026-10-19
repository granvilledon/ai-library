package com.book.backend.manager.model.request.function;

import java.io.Serializable;

/**
 * Function参数属性对象 ($.payload.functions.text.parameters.properties.*)
 */
public class SparkRequestFunctionProperty implements Serializable {

    private static final long serialVersionUID = -343415637582994606L;

    /**
     * 参数类型(必传)
     */
    private String type;

    /**
     * 参数详细描述(必传)
     */
    private String description;

    public SparkRequestFunctionProperty() {
    }

    public SparkRequestFunctionProperty(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
