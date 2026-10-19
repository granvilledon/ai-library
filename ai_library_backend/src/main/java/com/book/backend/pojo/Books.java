package com.book.backend.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 图书实体类
 */
@TableName(value = "books")
@Data
public class Books implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 图书ID
     */
    @TableId(type = IdType.AUTO)
    private Integer bookId;

    /**
     * 图书编号
     */
    private Long bookNumber;

    /**
     * 图书名称
     */
    private String bookName;

    /**
     * 图书作者
     */
    private String bookAuthor;

    /**
     * 所属图书馆
     */
    private String bookLibrary;

    /**
     * 图书分类
     */
    private String bookType;

    /**
     * 图书位置
     */
    private String bookLocation;

    /**
     * 图书状态
     */
    private String bookStatus;

    /**
     * 图书描述
     */
    private String bookDescription;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private String createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateTime;
}
