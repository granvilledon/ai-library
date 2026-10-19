package com.book.backend.pojo.vo;

import lombok.Data;

/**
 * BI图表分析响应结果
 */
@Data
public class BiResponse {

    /**
     * 生成的图表
     */
    private String genChart;

    /**
     * 分析结论
     */
    private String genResult;

    /**
     * 图表ID
     */
    private Long chartId;
}
