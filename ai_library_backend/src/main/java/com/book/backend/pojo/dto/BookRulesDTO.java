package com.book.backend.pojo.dto;

import com.book.backend.pojo.BookRules;
import lombok.Data;

import java.io.Serializable;

/**
 * 图书规则DTO，扩展复选框列表
 */
@Data
public class BookRulesDTO extends BookRules implements Serializable {
    /**
     * 复选框选中项
     */
    public String[] checkList;
}
