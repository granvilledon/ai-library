package com.book.backend.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统管理员实体类
 */
@TableName(value = "admins")
@Data
public class Admins implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 管理员ID
     */
    @TableId(type = IdType.AUTO)
    private Long adminId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码(MD5加密)
     */
    private String password;

    /**
     * 管理员真实姓名
     */
    private String adminName;

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
