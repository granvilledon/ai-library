package com.book.backend.common;

import lombok.Data;

/**
 * 分页基础类
 */
@Data
public class BasePage {
    /**
     * 当前页码
     */
    private int pageNum = 1;

    /**
     * 每页条数
     */
    private int pageSize = 3;

    /**
     * 搜索关键词
     */
    private String query;

    /**
     * 查询条件字段
     */
    private String condition;

    /**
     * 借阅证编号
     */
    private String cardNumber;
}
