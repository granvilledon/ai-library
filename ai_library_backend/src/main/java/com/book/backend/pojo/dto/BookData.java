package com.book.backend.pojo.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 图书Excel导入导出数据对象
 */
@Data
public class BookData implements Serializable {

    /**
     * 图书编号
     */
    @ExcelProperty(value = "图书编号", index = 0)
    private Long bookNumber;

    /**
     * 图书名称
     */
    @ExcelProperty(value = "书名", index = 1)
    private String bookName;

    /**
     * 图书作者
     */
    @ExcelProperty(value = "作者", index = 2)
    private String bookAuthor;

    /**
     * 所属图书馆
     */
    @ExcelProperty(value = "图书馆", index = 3)
    private String bookLibrary;

    /**
     * 图书分类
     */
    @ExcelProperty(value = "分类", index = 4)
    private String bookType;

    /**
     * 图书位置(如A12 B06)
     */
    @ExcelProperty(value = "位置", index = 5)
    private String bookLocation;

    /**
     * 图书状态(已借出/未借出)
     */
    @ExcelProperty(value = "状态", index = 6)
    private String bookStatus;

    /**
     * 图书描述
     */
    @ExcelProperty(value = "描述", index = 7)
    private String bookDescription;

}
