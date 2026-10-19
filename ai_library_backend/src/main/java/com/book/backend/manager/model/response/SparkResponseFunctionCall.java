package com.book.backend.manager.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;
import java.util.Map;

/**
 * Function调用响应对象
 */
public class SparkResponseFunctionCall implements Serializable {
    private static final long serialVersionUID = -1586729944571910329L;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String arguments;
    private String name;

    {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取解析后的参数Map
     *
     * @return 参数Map
     */
    public Map<String, Object> getMapArguments() {
        try {
            return objectMapper.readValue(arguments, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
