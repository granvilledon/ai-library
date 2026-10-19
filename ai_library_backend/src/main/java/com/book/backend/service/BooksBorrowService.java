package com.book.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.book.backend.common.BasePage;
import com.book.backend.common.R;
import com.book.backend.pojo.BookBorrows;
import com.book.backend.pojo.Violations;
import com.book.backend.pojo.dto.ViolationsDTO;

/**
 * 图书借阅记录服务接口
 */
public interface BooksBorrowService extends IService<BookBorrows> {

    /**
     * 查询借阅记录(分页+条件查询)
     *
     * @param basePage 分页参数和用户ID
     * @return 借阅记录分页数据
     */
    R<Page<BookBorrows>> getBookBorrowPage(BasePage basePage);

    /**
     * 查询图书逾期信息
     *
     * @param bookNumber 图书编号
     * @return 逾期信息
     */
    R<ViolationsDTO> queryExpireInformationByBookNumber(Long bookNumber);

    /**
     * 归还图书
     *
     * @param violations 违章记录
     * @return 操作结果
     */
    R<String> returnBook(Violations violations);

    /**
     * 获取还书报表
     *
     * @param basePage 分页参数和查询条件
     * @return 借阅记录分页数据
     */
    R<Page<BookBorrows>> getReturnStatement(BasePage basePage);
}
