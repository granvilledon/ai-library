package com.book.backend.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 图书管理员实体类
 */
@TableName(value = "book_admins")
@Data
public class BookAdmins implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 图书管理员ID
     */
    @TableId(type = IdType.AUTO)
    private Long bookAdminId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码(MD5加密)
     */
    private String password;

    /**
     * 图书管理员真实姓名
     */
    private String bookAdminName;

    /**
     * 状态(1-可用, 0-禁用)
     */
    private Integer status;

    /**
     * 电子邮箱
     */
    private String email;

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
