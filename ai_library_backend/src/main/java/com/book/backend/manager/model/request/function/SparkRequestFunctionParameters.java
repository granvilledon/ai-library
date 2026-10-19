package com.book.backend.manager.model.request.function;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Function参数配置对象
 */
public class SparkRequestFunctionParameters implements Serializable {

    private static final long serialVersionUID = 1079801431462837232L;

    /**
     * 参数类型(必传)
     */
    private String type;

    /**
     * 参数属性映射(必传)
     * key: 参数名称
     * value: 参数信息描述
     */
    private Map<String, SparkRequestFunctionProperty> properties;

    /**
     * 必填参数列表(必传)
     */
    private List<String> required;

    public SparkRequestFunctionParameters() {
    }

    public SparkRequestFunctionParameters(String type, Map<String, SparkRequestFunctionProperty> properties, List<String> required) {
        this.type = type;
        this.properties = properties;
        this.required = required;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, SparkRequestFunctionProperty> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, SparkRequestFunctionProperty> properties) {
        this.properties = properties;
    }

    public List<String> getRequired() {
        return required;
    }

    public void setRequired(List<String> required) {
        this.required = required;
    }
}
