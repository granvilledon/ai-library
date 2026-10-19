package com.book.backend.pojo.dto;

import com.book.backend.pojo.Users;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户DTO，扩展用户状态文本
 */
@Data
public class UsersDTO extends Users implements Serializable {
    /**
     * 用户状态文本(可用/禁用)
     */
    public String userStatus;
}
