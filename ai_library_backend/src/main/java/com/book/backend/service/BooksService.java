package com.book.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.book.backend.common.BasePage;
import com.book.backend.common.R;
import com.book.backend.pojo.Books;
import com.book.backend.pojo.dto.BookBorrowsDTO;
import com.book.backend.pojo.dto.BookDTO;
import com.book.backend.pojo.dto.BorrowTypeDTO;

import java.util.List;

/**
 * 图书服务接口
 */
public interface BooksService extends IService<Books> {

    /**
     * 查询图书(分页+模糊查询)
     *
     * @param basePage 分页参数和查询条件
     * @return 图书分页数据
     */
    R<Page<Books>> searchBookPage(BasePage basePage);

    /**
     * 借阅图书
     *
     * @param booksBorrowDTO 借阅信息
     * @return 操作结果
     */
    R<String> borrowBookByCardNumberAndBookNumber(BookBorrowsDTO booksBorrowDTO);

    /**
     * 查询图书状态(是否借出/逾期)
     *
     * @param bookNumber 图书编号
     * @return 图书状态
     */
    R<String> queryBookExpireByBookNumber(Long bookNumber);

    /**
     * 获取图书列表(分页)
     *
     * @param basePage 分页参数和查询条件
     * @return 图书分页数据
     */
    R<Page<Books>> getBookList(BasePage basePage);

    /**
     * 添加图书
     *
     * @param bookDTO 图书信息(含分类ID)
     * @return 操作结果
     */
    R<String> addBook(BookDTO bookDTO);

    /**
     * 删除图书
     *
     * @param bookId 图书ID
     * @return 操作结果
     */
    R<String> deleteBookByBookId(Integer bookId);

    /**
     * 根据ID获取图书信息
     *
     * @param bookId 图书ID
     * @return 图书信息
     */
    R<Books> getBookInformationByBookId(Integer bookId);

    /**
     * 修改图书信息
     *
     * @param books 图书信息
     * @return 操作结果
     */
    R<String> updateBookByEditForm(Books books);

    /**
     * 获取图书分类借阅统计
     *
     * @return 分类统计数据
     */
    R<List<BorrowTypeDTO>> getBorrowTypeStatistic();

    /**
     * 批量删除图书
     *
     * @param booksList 图书列表
     * @return 操作结果
     */
    R<String> deleteBookByBatch(List<Books> booksList);

    /**
     * 根据编号查询图书
     *
     * @param bookNumber 图书编号
     * @return 图书信息
     */
    Books getBookByBookNumber(Long bookNumber);

}