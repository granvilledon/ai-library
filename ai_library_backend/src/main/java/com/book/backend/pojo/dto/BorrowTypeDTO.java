package com.book.backend.pojo.dto;

import lombok.Data;

/**
 * 图书分类借阅统计DTO
 */
@Data
public class BorrowTypeDTO {
    /**
     * 图书分类
     */
    public String bookTypes;

    /**
     * 借阅量
     */
    public Integer borrowNumbers;
}
