package com.book.backend.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户实体类
 */
@TableName(value = "users")
@Data
public class Users implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码(MD5加密)
     */
    private String password;

    /**
     * 真实姓名
     */
    private String cardName;

    /**
     * 借阅证编号(11位)
     */
    private Long cardNumber;

    /**
     * 规则编号(权限等级)
     */
    private Integer ruleNumber;

    /**
     * 状态(1-可用, 0-禁用)
     */
    private Integer status;

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
