package com.book.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.book.backend.common.BasePage;
import com.book.backend.common.R;
import com.book.backend.pojo.Users;
import com.book.backend.pojo.dto.UsersDTO;

/**
 * 用户服务接口
 */
public interface UsersService extends IService<Users> {

    /**
     * 根据ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    R<Users> getUserByUserId(Integer userId);

    /**
     * 修改密码
     *
     * @param users 用户信息(含新密码)
     * @return 操作结果
     */
    R<String> updatePassword(Users users);

    /**
     * 用户登录
     *
     * @param users 用户信息
     * @return 登录结果(含Token)
     */
    R login(Users users);

    /**
     * 获取用户数据
     *
     * @param users 用户信息
     * @return 用户数据
     */
    R<Users> getUserData(Users users);

    /**
     * 获取用户列表(分页)
     *
     * @param basePage 分页参数和查询条件
     * @return 用户分页数据
     */
    R<Page<Users>> getStatementList(BasePage basePage);

    /**
     * 根据ID获取用户信息(用于回显)
     *
     * @param userId 用户ID
     * @return 用户DTO(含状态文本)
     */
    R<UsersDTO> getStatementByUserId(Integer userId);

    /**
     * 修改用户信息
     *
     * @param usersDTO 用户DTO
     * @return 操作结果
     */
    R<String> updateStatement(UsersDTO usersDTO);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    R<String> deleteStatementByUserId(Integer userId);
}
