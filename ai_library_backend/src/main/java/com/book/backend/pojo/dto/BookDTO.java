package com.book.backend.pojo.dto;

import com.book.backend.pojo.Books;
import lombok.Data;

/**
 * 图书DTO，扩展图书分类编号
 */
@Data
public class BookDTO extends Books {
    /**
     * 图书分类ID
     */
    public Integer bookTypeNumber;

}
