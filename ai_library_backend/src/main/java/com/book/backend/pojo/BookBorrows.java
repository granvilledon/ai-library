package com.book.backend.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 图书借阅记录实体类
 */
@TableName(value = "book_borrows")
@Data
public class BookBorrows implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 借阅记录ID
     */
    @TableId(type = IdType.AUTO)
    private Integer borrowId;

    /**
     * 借阅证编号(11位)
     */
    private Long cardNumber;

    /**
     * 图书编号
     */
    private Long bookNumber;

    /**
     * 借阅日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime borrowDate;

    /**
     * 应还日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime closeDate;

    /**
     * 实际归还日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime returnDate;

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

    public BookBorrows(Integer borrowId, Long cardNumber, Long bookNumber, LocalDateTime borrowDate, LocalDateTime closeDate, LocalDateTime returnDate, String createTime, String updateTime) {
        this.borrowId = borrowId;
        this.cardNumber = cardNumber;
        this.bookNumber = bookNumber;
        this.borrowDate = borrowDate;
        this.closeDate = closeDate;
        this.returnDate = returnDate;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public BookBorrows() {
    }
}
