package com.book.backend.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 评论弹幕DTO
 */
@Data
public class CommentDTO implements Serializable {
    /**
     * 评论ID
     */
    public Integer id;

    /**
     * 弹幕内容
     */
    public String msg;

    /**
     * 显示时间
     */
    public Integer time;

    /**
     * 弹幕样式
     */
    public String barrageStyle;
}
