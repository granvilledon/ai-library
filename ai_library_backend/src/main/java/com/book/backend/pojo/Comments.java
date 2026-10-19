package com.book.backend.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 评论弹幕实体类
 */
@TableName(value = "comments")
@Data
public class Comments implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    @TableId(type = IdType.AUTO)
    private Integer commentId;

    /**
     * 弹幕高度样式
     */
    private String commentBarrageStyle;

    /**
     * 弹幕内容
     */
    private String commentMessage;

    /**
     * 显示时间(控制速度)
     */
    private Integer commentTime;

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
