package com.book.backend.pojo.dto;

import com.book.backend.pojo.BookBorrows;
import lombok.Data;

import java.io.Serializable;

/**
 * 图书借阅DTO，扩展图书管理员ID
 */
@Data
public class BookBorrowsDTO extends BookBorrows implements Serializable {
    /**
     * 图书管理员ID
     */
    public Integer bookAdminId;

}
