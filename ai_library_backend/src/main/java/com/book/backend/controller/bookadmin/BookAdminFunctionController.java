package com.book.backend.controller.bookadmin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.book.backend.common.BasePage;
import com.book.backend.common.R;
import com.book.backend.pojo.BookBorrows;
import com.book.backend.pojo.Notices;
import com.book.backend.pojo.Violations;
import com.book.backend.pojo.dto.BookBorrowsDTO;
import com.book.backend.pojo.dto.ViolationsDTO;
import com.book.backend.service.BookAdminsService;
import com.book.backend.service.BooksBorrowService;
import com.book.backend.service.BooksService;
import com.book.backend.service.NoticeService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 图书管理员功能控制器，提供借还书、报表查询、公告管理等功能
 */
@RestController
@RequestMapping("/bookadmin")
public class BookAdminFunctionController {
    @Resource
    private BookAdminsService bookAdminsService;

    @Resource
    private BooksService booksService;
    @Resource
    private BooksBorrowService booksBorrowService;

    @Resource
    private NoticeService noticeService;


    /**
     * 借阅图书，根据借阅证号和图书编号
     *
     * @param booksBorrowDTO 借阅信息DTO
     * @return 操作结果
     */
    @PostMapping("borrowBook")
    @ApiOperation("借阅图书")
    public R<String> borrowBookByCardNumberAndBookNumber(@RequestBody BookBorrowsDTO booksBorrowDTO) {
        return booksService.borrowBookByCardNumberAndBookNumber(booksBorrowDTO);
    }

    /**
     * 查询图书是否已借出
     *
     * @param bookNumber 图书编号
     * @return 图书状态
     */
    @GetMapping("queryBookStatus/{bookNumber}")
    @ApiOperation("查询图书状态")
    public R<String> queryBookExpireByBookNumber(@PathVariable("bookNumber") Long bookNumber) {
        return booksService.queryBookExpireByBookNumber(bookNumber);
    }

    /**
     * 查询图书逾期信息
     *
     * @param bookNumber 图书编号
     * @return 逾期信息DTO
     */
    @GetMapping("queryOverdueInfo/{bookNumber}")
    @ApiOperation("查询逾期信息")
    public R<ViolationsDTO> queryExpireInformationByBookNumber(@PathVariable("bookNumber") Long bookNumber) {
        return booksBorrowService.queryExpireInformationByBookNumber(bookNumber);
    }

    /**
     * 归还图书
     *
     * @param violations 违章记录
     * @return 操作结果
     */
    @PostMapping("returnBook")
    @ApiOperation("归还图书")
    public R<String> returnBook(@RequestBody Violations violations) {
        return booksBorrowService.returnBook(violations);
    }

    /**
     * 获取还书记录列表(分页+搜索)
     *
     * @param basePage 分页参数和搜索条件
     * @return 分页还书记录
     */
    @PostMapping("getReturnRecords")
    @ApiOperation("获取还书记录")
    public R<Page<BookBorrows>> getReturnRecords(@RequestBody BasePage basePage) {
        return booksBorrowService.getReturnStatement(basePage);
    }

    /**
     * 获取借书记录列表(分页+搜索)
     *
     * @param basePage 分页参数和搜索条件
     * @return 分页借书记录
     */
    @PostMapping("getBorrowRecords")
    @ApiOperation("获取借书记录")
    public R<Page<ViolationsDTO>> getBorrowRecords(@RequestBody BasePage basePage) {
        return bookAdminsService.getBorrowStatement(basePage);
    }

    /**
     * 获取公告列表(分页)
     *
     * @param basePage 分页参数
     * @return 分页公告列表
     */
    @PostMapping("getNoticeList")
    @ApiOperation("获取公告列表")
    public R<Page<Notices>> getNoticeList(@RequestBody BasePage basePage) {
        return noticeService.getNoticeList(basePage);
    }

    /**
     * 添加公告
     *
     * @param notices 公告信息
     * @return 操作结果
     */
    @PostMapping("addNotice")
    @ApiOperation("添加公告")
    public R<String> addNotice(@RequestBody Notices notices) {
        return noticeService.addNotice(notices);
    }

    /**
     * 根据ID删除公告
     *
     * @param noticeId 公告ID
     * @return 操作结果
     */
    @DeleteMapping("deleteNotice/{noticeId}")
    @ApiOperation("删除公告")
    public R<String> deleteNoticeById(@PathVariable("noticeId") Integer noticeId) {
        return noticeService.deleteNoticeById(noticeId);
    }

    /**
     * 根据ID查询公告
     *
     * @param noticeId 公告ID
     * @return 公告信息
     */
    @GetMapping("getNotice/{noticeId}")
    @ApiOperation("查询公告")
    public R<Notices> getNoticeByNoticeId(@PathVariable("noticeId") Integer noticeId) {
        return noticeService.getNoticeByNoticeId(noticeId);
    }

    /**
     * 更新公告
     *
     * @param noticeId 公告ID
     * @param notices  公告信息
     * @return 操作结果
     */
    @PutMapping("updateNotice/{noticeId}")
    @ApiOperation("更新公告")
    public R<String> updateNoticeByNoticeId(@PathVariable("noticeId") Integer noticeId, @RequestBody Notices notices) {
        return noticeService.updateNoticeByNoticeId(noticeId, notices);
    }
}
