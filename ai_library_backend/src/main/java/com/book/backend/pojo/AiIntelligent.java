package com.book.backend.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/**
 * AI智能分析实体类
 */
@TableName(value = "ai_intelligent")
@Data
public class AiIntelligent implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户输入内容
     */
    private String inputMessage;

    /**
     * AI生成结果
     */
    private String aiResult;

    /**
     * 用户ID(可为空)
     */
    private Long userId;

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
