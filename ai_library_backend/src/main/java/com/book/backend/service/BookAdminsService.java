package com.book.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.book.backend.common.BasePage;
import com.book.backend.common.R;
import com.book.backend.pojo.BookAdmins;
import com.book.backend.pojo.dto.ViolationsDTO;

/**
 * 图书管理员服务接口
 */
public interface BookAdminsService extends IService<BookAdmins> {

    /**
     * 添加图书管理员
     *
     * @param bookAdmins 管理员信息
     * @return 操作结果
     */
    R<String> addBookAdmin(BookAdmins bookAdmins);

    /**
     * 图书管理员登录
     *
     * @param bookAdmins 管理员信息
     * @return 登录结果(含Token)
     */
    R login(BookAdmins bookAdmins);

    /**
     * 获取管理员数据
     *
     * @param bookAdmins 管理员信息
     * @return 管理员数据
     */
    R<BookAdmins> getUserData(BookAdmins bookAdmins);

    /**
     * 获取借书报表
     *
     * @param basePage 分页参数和查询条件
     * @return 违章记录分页数据
     */
    R<Page<ViolationsDTO>> getBorrowStatement(BasePage basePage);

    /**
     * 获取图书管理员列表(分页)
     *
     * @param basePage 分页参数
     * @return 管理员分页数据
     */
    R<Page<BookAdmins>> getBookAdminListByPage(BasePage basePage);

    /**
     * 根据ID获取管理员信息
     *
     * @param bookAdminId 管理员ID
     * @return 管理员信息
     */
    R<BookAdmins> getBookAdminById(Integer bookAdminId);

    /**
     * 删除管理员
     *
     * @param bookAdminId 管理员ID
     * @return 操作结果
     */
    R<String> deleteBookAdminById(Integer bookAdminId);

    /**
     * 修改管理员信息
     *
     * @param bookAdmins 管理员信息
     * @return 操作结果
     */
    R<String> updateBookAdmin(BookAdmins bookAdmins);
}
