package com.book.backend.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 图书借阅规则实体类
 */
@TableName(value = "book_rules")
@Data
public class BookRules implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 规则ID
     */
    @TableId(type = IdType.AUTO)
    private Integer ruleId;

    /**
     * 规则编号
     */
    private Integer bookRuleId;

    /**
     * 借阅天数
     */
    private Integer bookDays;

    /**
     * 最大借阅数量
     */
    private Integer bookLimitNumber;

    /**
     * 适用图书馆
     */
    private String bookLimitLibrary;

    /**
     * 逾期费用(元/天)
     */
    private Double bookOverdueFee;

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
