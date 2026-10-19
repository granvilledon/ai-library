package com.book.backend.manager.model.request.function;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Function调用构建器
 */
public class SparkFunctionBuilder {

    private final SparkRequestFunctionMessage sparkRequestFunctionMessage;

    public SparkFunctionBuilder() {
        sparkRequestFunctionMessage = new SparkRequestFunctionMessage();
        sparkRequestFunctionMessage.setParameters(new SparkRequestFunctionParameters("object", new LinkedHashMap<>(), new ArrayList<>()));
    }

    /**
     * 创建Function构建器并设置名称
     *
     * @param name Function名称
     * @return SparkFunctionBuilder实例
     */
    public static SparkFunctionBuilder functionName(String name) {
        return new SparkFunctionBuilder().name(name);
    }

    /**
     * 设置Function名称(必传)
     *
     * @param name Function名称，用户输入命中后返回该名称
     * @return SparkFunctionBuilder实例
     */
    public SparkFunctionBuilder name(String name) {
        sparkRequestFunctionMessage.setName(name);
        return this;
    }

    /**
     * 设置Function功能描述(必传)
     *
     * @param description 功能描述，越详细越有助于大模型理解
     * @return SparkFunctionBuilder实例
     */
    public SparkFunctionBuilder description(String description) {
        sparkRequestFunctionMessage.setDescription(description);
        return this;
    }

    /**
     * 设置参数类型(必传)
     *
     * @param type 参数类型，默认值：object
     * @return SparkFunctionBuilder实例
     */
    public SparkFunctionBuilder parameterType(String type) {
        sparkRequestFunctionMessage.getParameters().setType(type);
        return this;
    }

    /**
     * 添加参数属性(必传)
     *
     * @param name        参数名称
     * @param type        参数类型
     * @param description 参数描述
     * @return SparkFunctionBuilder实例
     */
    public SparkFunctionBuilder addParameterProperty(String name, String type, String description) {
        sparkRequestFunctionMessage.getParameters().getProperties().put(name, new SparkRequestFunctionProperty(type, description));
        return this;
    }

    /**
     * 添加必填参数
     *
     * @param name 参数名称列表
     * @return SparkFunctionBuilder实例
     */
    public SparkFunctionBuilder addParameterRequired(String... name) {
        for (String s : name) {
            sparkRequestFunctionMessage.getParameters().getRequired().add(s);
        }
        return this;
    }

    /**
     * 设置完整参数配置
     *
     * @param type       参数类型
     * @param properties 参数属性映射
     * @param required   必填参数列表
     * @return SparkFunctionBuilder实例
     */
    public SparkFunctionBuilder parameters(String type, Map<String, SparkRequestFunctionProperty> properties, List<String> required) {
        sparkRequestFunctionMessage.setParameters(new SparkRequestFunctionParameters(type, properties, required));
        return this;
    }

    /**
     * 构建Function消息对象
     *
     * @return SparkRequestFunctionMessage实例
     */
    public SparkRequestFunctionMessage build() {
        return sparkRequestFunctionMessage;
    }
}
