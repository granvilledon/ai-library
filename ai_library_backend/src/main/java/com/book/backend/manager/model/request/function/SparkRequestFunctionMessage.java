package com.book.backend.manager.model.request.function;

import java.io.Serializable;

/**
 * Function调用消息对象 ($.payload.functions.text)
 */
public class SparkRequestFunctionMessage implements Serializable {

    private static final long serialVersionUID = 6587302404547694700L;

    /**
     * Function名称(必传)，用户输入命中后返回该名称
     */
    private String name;

    /**
     * Function功能描述(必传)，描述越详细越有助于大模型理解
     */
    private String description;

    /**
     * Function参数列表(必传)
     */
    private SparkRequestFunctionParameters parameters;

    public SparkRequestFunctionMessage() {
    }

    public SparkRequestFunctionMessage(String name, String description, SparkRequestFunctionParameters parameters) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SparkRequestFunctionParameters getParameters() {
        return parameters;
    }

    public void setParameters(SparkRequestFunctionParameters parameters) {
        this.parameters = parameters;
    }
}
