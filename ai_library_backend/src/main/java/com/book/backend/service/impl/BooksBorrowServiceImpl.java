package com.book.backend.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book.backend.common.BasePage;
import com.book.backend.common.R;
import com.book.backend.constant.Constant;
import com.book.backend.mapper.BooksBorrowMapper;
import com.book.backend.pojo.BookBorrows;
import com.book.backend.pojo.Books;
import com.book.backend.pojo.Violations;
import com.book.backend.pojo.dto.ViolationsDTO;
import com.book.backend.service.BooksBorrowService;
import com.book.backend.service.BooksService;
import com.book.backend.service.ViolationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 图书借阅记录服务实现类
 */
@Service
public class BooksBorrowServiceImpl extends ServiceImpl<BooksBorrowMapper, BookBorrows>
        implements BooksBorrowService {

    @Resource
    private ViolationService violationService;

    private BooksService booksService;

    @Autowired
    public BooksBorrowServiceImpl(@Lazy BooksService booksService) {
        this.booksService = booksService;
    }

    /**
     * 获取用户借阅记录（分页）
     *
     * @param basePage 分页参数（包含cardNumber，支持按条件搜索）
     * @return 分页的借阅记录列表
     */
    @Override
    public R<Page<BookBorrows>> getBookBorrowPage(BasePage basePage) {
        String cardNumberString = basePage.getCardNumber();
        long cardNumber = Long.parseLong(cardNumberString);
        R<Page<BookBorrows>> result = new R<>();
        QueryWrapper<BookBorrows> queryWrapper = new QueryWrapper<>();
        int pageNum = basePage.getPageNum();
        int pageSize = basePage.getPageSize();
        Page<BookBorrows> pageInfo = new Page<>(pageNum, pageSize);
        queryWrapper.eq("card_number", cardNumber);
        List<BookBorrows> list = this.list(queryWrapper);
        if (list.isEmpty()) {
            return R.error("获取不到该用户借阅信息");
        }
        String condition = basePage.getCondition();
        String query = basePage.getQuery();
        if (StringUtils.isBlank(condition) || StringUtils.isBlank(query)) {
            LambdaQueryWrapper<BookBorrows> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(BookBorrows::getCardNumber, cardNumber).orderByAsc(BookBorrows::getCreateTime);
            this.page(pageInfo, queryWrapper1);
            result.setData(pageInfo);
            result.setStatus(200);
            result.setMsg("获取借阅信息成功");
            return result;
        }
        queryWrapper.like(condition, query);
        Page<BookBorrows> page = this.page(pageInfo, queryWrapper);
        if (page.getTotal() == 0) {
            return R.error("查询不到该借阅信息");
        }
        result.setData(pageInfo);
        result.setStatus(200);
        result.setMsg("获取借阅信息成功");
        return result;
    }

    /**
     * 查询图书逾期信息
     *
     * @param bookNumber 图书编号
     * @return 逾期信息DTO，包含逾期天数、应还日期等
     */
    @Override
    public R<ViolationsDTO> queryExpireInformationByBookNumber(Long bookNumber) {
        LambdaQueryWrapper<BookBorrows> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BookBorrows::getBookNumber, bookNumber).isNull(BookBorrows::getReturnDate);
        BookBorrows bookBorrowRecord = this.getOne(queryWrapper);
        if (bookBorrowRecord == null) {
            return R.error("获取逾期信息失败");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime closeDate = bookBorrowRecord.getCloseDate();
        String nowFormat = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String closeFormat = closeDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String[] s = nowFormat.split(" ");
        String[] s1 = closeFormat.split(" ");
        LocalDateTime borrow = LocalDateTimeUtil.parse(s[0] + "T" + s[1]);
        LocalDateTime close = LocalDateTimeUtil.parse(s1[0] + "T" + s1[1]);
        Duration between = LocalDateTimeUtil.between(borrow, close);
        long expireDay = between.toDays();
        ViolationsDTO violationDTO = new ViolationsDTO();
        violationDTO.setExpireDays(expireDay);
        violationDTO.setBookNumber(bookNumber);
        violationDTO.setCloseDate(closeDate);
        R<ViolationsDTO> result = new R<>();
        result.setData(violationDTO);
        result.setStatus(200);
        result.setMsg("获取逾期信息成功");
        return result;
    }

    /**
     * 归还图书
     *
     * @param violations 归还信息（包含图书编号、归还日期、违规信息等）
     * @return 操作结果，成功返回"归还图书成功"
     */
    @Transactional
    @Override
    public R<String> returnBook(Violations violations) {
        Long bookNumber = violations.getBookNumber();
        LocalDateTime returnDate = violations.getReturnDate();
        if (returnDate == null) {
            return R.error("归还日期不能为空");
        }
        String violationMessage = violations.getViolationMessage();
        Integer violationAdminId = violations.getViolationAdminId();
        LambdaQueryWrapper<Violations> queryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<BookBorrows> queryWrapper1 = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Books> queryWrapper2 = new LambdaQueryWrapper<>();

        queryWrapper.eq(Violations::getBookNumber, bookNumber).isNull(Violations::getReturnDate);
        queryWrapper1.eq(BookBorrows::getBookNumber, bookNumber).isNull(BookBorrows::getReturnDate);
        queryWrapper2.eq(Books::getBookNumber, bookNumber);
        Violations violations1 = violationService.getOne(queryWrapper);
        BookBorrows bookBorrows = this.getOne(queryWrapper1);
        Books book = booksService.getOne(queryWrapper2);
        if (violations1 == null || bookBorrows == null || book == null) {
            return R.error("归还图书失败");
        }

        violations1.setViolationMessage(violationMessage);
        violations1.setReturnDate(returnDate);
        violations1.setViolationAdminId(violationAdminId);
        bookBorrows.setReturnDate(returnDate);
        book.setBookStatus(Constant.BOOKAVAILABLE);
        boolean update1 = violationService.update(violations1, queryWrapper);
        boolean update2 = this.update(bookBorrows, queryWrapper1);
        boolean update3 = booksService.update(book, queryWrapper2);
        if (!update1 || !update2 || !update3) {
            return R.error("归还图书失败");
        }

        return R.success(null, "归还图书成功");
    }

    /**
     * 获取还书报表（未归还记录）
     *
     * @param basePage 分页参数（支持按条件搜索）
     * @return 分页的未归还借阅记录列表
     */
    @Override
    public R<Page<BookBorrows>> getReturnStatement(BasePage basePage) {
        int pageNum = basePage.getPageNum();
        int pageSize = basePage.getPageSize();
        String query = basePage.getQuery();
        String condition = basePage.getCondition();
        Page<BookBorrows> pageInfo = new Page<>(pageNum, pageSize);
        R<Page<BookBorrows>> result = new R<>();
        if (StringUtils.isBlank(condition) || StringUtils.isBlank(query)) {
            LambdaQueryWrapper<BookBorrows> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.isNull(BookBorrows::getReturnDate);
            Page<BookBorrows> page = this.page(pageInfo, queryWrapper);
            if (page.getTotal() == 0) {
                return R.error("还书报表信息为空");
            }
            result.setData(pageInfo);
            result.setStatus(200);
            result.setMsg("获取还书报表信息成功");
            return result;
        }
        QueryWrapper<BookBorrows> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.like(condition, query).isNull("return_date").orderByAsc("borrow_date");
        Page<BookBorrows> page = this.page(pageInfo, queryWrapper1);
        if (page.getTotal() == 0) {
            return R.error("查询不到该还书报表信息");
        }
        result.setData(pageInfo);
        result.setMsg("获取还书报表信息成功");
        result.setStatus(200);
        return result;
    }
}