package com.book.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book.backend.common.BasePage;
import com.book.backend.common.R;
import com.book.backend.common.exception.CommonError;
import com.book.backend.common.exception.VueBookException;
import com.book.backend.constant.Constant;
import com.book.backend.mapper.BooksMapper;
import com.book.backend.pojo.*;
import com.book.backend.pojo.dto.BookBorrowsDTO;
import com.book.backend.pojo.dto.BookDTO;
import com.book.backend.pojo.dto.BorrowTypeDTO;
import com.book.backend.service.*;
import com.book.backend.utils.NumberUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 图书服务实现类
 */
@Service
public class BooksServiceImpl extends ServiceImpl<BooksMapper, Books>
        implements BooksService {

    @Resource
    private UsersService usersService;

    @Resource
    private BookRuleService bookRuleService;

    @Resource
    private ViolationService violationService;

    @Resource
    private BookTypeService bookTypeService;

    private BooksBorrowService booksBorrowService;

    @Autowired
    public BooksServiceImpl(@Lazy BooksBorrowService booksBorrowService) {
        this.booksBorrowService = booksBorrowService;
    }

    /**
     * 搜索图书（分页）
     *
     * @param basePage 分页参数（包含condition和query，支持按书名、作者、类型、图书馆、位置搜索）
     * @return 分页的图书列表
     */
    @Override
    public R<Page<Books>> searchBookPage(BasePage basePage) {
        String condition = basePage.getCondition();
        String query = basePage.getQuery();
        int pageSize = basePage.getPageSize();
        int pageNum = basePage.getPageNum();
        Page<Books> pageInfo = new Page<>(pageNum, pageSize);
        R<Page<Books>> result = new R<>();

        LambdaQueryWrapper<Books> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(condition) && StringUtils.isNotBlank(query)) {
            switch (condition) {
                case "book_number":
                    queryWrapper.like(Books::getBookNumber, query);
                    break;
                case "book_name":
                    queryWrapper.like(Books::getBookName, query);
                    break;
                case "book_author":
                    queryWrapper.like(Books::getBookAuthor, query);
                    break;
                case "book_type":
                    queryWrapper.like(Books::getBookType, query);
                    break;
                case "book_library":
                    queryWrapper.like(Books::getBookLibrary, query);
                    break;
                case "book_location":
                    queryWrapper.like(Books::getBookLocation, query);
                    break;
                case "book_status":
                    queryWrapper.eq(Books::getBookStatus, query);
                    break;
                case "book_description":
                    queryWrapper.like(Books::getBookDescription, query);
                    break;
                default:
                    result.setMsg("无效的查询字段");
                    result.setStatus(400);
                    return result;
            }
        }

        queryWrapper.orderByAsc(Books::getBookId);
        this.page(pageInfo, queryWrapper);
        result.setData(pageInfo);
        result.setStatus(200);
        result.setMsg("获取图书信息成功");
        return result;
    }

    /**
     * 借阅图书
     *
     * @param booksBorrowDTO 借阅信息（包含图书编号、读者卡号、借阅日期等）
     * @return 操作结果，成功返回"借阅图书成功"
     */
    @Transactional
    @Override
    public R<String> borrowBookByCardNumberAndBookNumber(BookBorrowsDTO booksBorrowDTO) {
        Long bookNumber = booksBorrowDTO.getBookNumber();
        Long cardNumber = booksBorrowDTO.getCardNumber();
        LocalDateTime borrowDate = booksBorrowDTO.getBorrowDate();
        if (borrowDate == null) {
            return R.error("借阅时间不能为空");
        }

        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Users::getCardNumber, cardNumber);
        Users users = usersService.getOne(queryWrapper);
        if (users == null) {
            return R.error("借阅图书失败");
        }

        LambdaQueryWrapper<Books> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Books::getBookNumber, bookNumber);
        Books book = this.getOne(queryWrapper1);
        if ((book == null) || (book.getBookStatus().equals(Constant.BOOKDISABLE))) {
            VueBookException.cast(CommonError.QUERY_NULL);
        }

        Integer ruleNumber = users.getRuleNumber();
        LambdaQueryWrapper<BookRules> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(BookRules::getBookRuleId, ruleNumber);
        BookRules bookRules = bookRuleService.getOne(queryWrapper2);
        if (bookRules == null) {
            return R.error("借阅图书失败");
        }

        Integer bookDays = bookRules.getBookDays();
        LocalDateTime closeDate = borrowDate.plusDays(bookDays);
        BookBorrows bookBorrows1 = new BookBorrows();
        bookBorrows1.setBorrowId(null);
        bookBorrows1.setBookNumber(bookNumber);
        bookBorrows1.setCardNumber(cardNumber);
        bookBorrows1.setBorrowDate(borrowDate);
        bookBorrows1.setCloseDate(closeDate);
        bookBorrows1.setReturnDate(null);
        boolean flag = booksBorrowService.save(bookBorrows1);
        if (!flag) {
            return R.error("借阅图书失败");
        }

        book.setBookStatus(Constant.BOOKDISABLE);
        boolean update = this.update(book, queryWrapper1);
        if (!update) {
            return R.error("借阅图书失败");
        }

        Violations violations = new Violations();
        BeanUtils.copyProperties(bookBorrows1, violations, "borrowId");
        violations.setViolationId(null);
        violations.setViolationMessage("");
        violations.setViolationAdminId(booksBorrowDTO.getBookAdminId());
        boolean save = violationService.save(violations);
        if (!save) {
            return R.error("借阅图书失败");
        }
        return R.success(null, "借阅图书成功");
    }

    /**
     * 查询图书是否已借出
     *
     * @param bookNumber 图书编号
     * @return 图书状态，已借出返回"图书已借出"，否则返回错误信息
     */
    @Override
    public R<String> queryBookExpireByBookNumber(Long bookNumber) {
        LambdaQueryWrapper<Books> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Books::getBookNumber, bookNumber);
        Books book = this.getOne(queryWrapper);
        if (book == null || book.getBookStatus().equals(Constant.BOOKAVAILABLE)) {
            return R.error("该图书未借出或图书编号不存在");
        }
        return R.success(null, "图书已借出");
    }

    /**
     * 根据图书编号获取图书
     *
     * @param bookNumber 图书编号
     * @return 图书对象，不存在返回null
     */
    @Override
    public Books getBookByBookNumber(Long bookNumber) {
        LambdaQueryWrapper<Books> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Books::getBookNumber, bookNumber);
        return this.getOne(queryWrapper);
    }

    /**
     * 获取图书列表（分页）
     *
     * @param basePage 分页参数（支持按条件搜索）
     * @return 分页的图书列表，按图书编号升序排列
     */
    @Override
    public R<Page<Books>> getBookList(BasePage basePage) {
        int pageNum = basePage.getPageNum();
        int pageSize = basePage.getPageSize();
        String query = basePage.getQuery();
        String condition = basePage.getCondition();
        Page<Books> pageInfo = new Page<>(pageNum, pageSize);
        R<Page<Books>> result = new R<>();

        LambdaQueryWrapper<Books> queryWrapper1 = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(condition) && StringUtils.isNotBlank(query)) {
            switch (condition) {
                case "book_number":
                    queryWrapper1.like(Books::getBookNumber, query);
                    break;
                case "book_name":
                    queryWrapper1.like(Books::getBookName, query);
                    break;
                case "book_author":
                    queryWrapper1.like(Books::getBookAuthor, query);
                    break;
                case "book_type":
                    queryWrapper1.like(Books::getBookType, query);
                    break;
                case "book_library":
                    queryWrapper1.like(Books::getBookLibrary, query);
                    break;
                case "book_location":
                    queryWrapper1.like(Books::getBookLocation, query);
                    break;
                case "book_status":
                    queryWrapper1.eq(Books::getBookStatus, query);
                    break;
                case "book_description":
                    queryWrapper1.like(Books::getBookDescription, query);
                    break;
                default:
                    result.setMsg("无效的查询字段");
                    result.setStatus(400);
                    return result;
            }
        }

        queryWrapper1.orderByAsc(Books::getBookNumber);
        Page<Books> page = this.page(pageInfo, queryWrapper1);
        if (page.getTotal() == 0) {
            return R.error("图书列表为空");
        }
        result.setData(pageInfo);
        result.setStatus(200);
        result.setMsg("获取图书列表成功");
        return result;
    }

    /**
     * 添加图书
     *
     * @param bookDTO 图书信息DTO（包含图书类型编号，图书编号自动生成）
     * @return 操作结果，成功返回"添加图书信息成功"
     */
    @Transactional
    @Override
    public R<String> addBook(BookDTO bookDTO) {
        Integer bookTypeNumber = bookDTO.getBookTypeNumber();
        LambdaQueryWrapper<BookTypes> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BookTypes::getTypeId, bookTypeNumber);
        BookTypes bookTypes = bookTypeService.getOne(queryWrapper);
        if (bookTypes == null) {
            return R.error("添加图书失败");
        }

        String typeName = bookTypes.getTypeName();
        StringBuilder stringNumber = NumberUtil.getNumber(11);
        long bookNumber = Long.parseLong(new String(stringNumber));
        Books books = new Books();
        BeanUtils.copyProperties(bookDTO, books, "book_type");
        books.setBookType(typeName);
        books.setBookNumber(bookNumber);
        boolean save = this.save(books);
        if (!save) {
            return R.error("添加图书失败");
        }

        return R.success(null, "添加图书信息成功");
    }

    /**
     * 删除图书
     *
     * @param bookId 图书ID
     * @return 操作结果，成功返回"删除图书成功"
     */
    @Transactional
    @Override
    public R<String> deleteBookByBookId(Integer bookId) {
        Books books = this.getById(bookId);
        if (books == null) {
            return R.error("删除图书失败");
        }
        boolean remove = this.removeById(bookId);
        if (!remove) {
            return R.error("删除图书失败");
        }
        return R.success(null, "删除图书成功");
    }

    /**
     * 根据ID获取图书信息
     *
     * @param bookId 图书ID
     * @return 图书对象
     */
    @Override
    public R<Books> getBookInformationByBookId(Integer bookId) {
        Books books = this.getById(bookId);
        if (books == null) {
            return R.error("获取图书信息错误");
        }
        R<Books> result = new R<>();
        result.setStatus(200);
        result.setData(books);
        result.setMsg("获取图书信息成功");
        return result;
    }

    /**
     * 更新图书信息
     *
     * @param books 图书对象
     * @return 操作结果，成功返回"修改图书成功"
     */
    @Override
    public R<String> updateBookByEditForm(Books books) {
        if (books == null) {
            return R.error("修改图书失败");
        }
        boolean update = this.updateById(books);
        if (!update) {
            return R.error("修改图书失败");
        }
        return R.success(null, "修改图书成功");
    }

    /**
     * 获取借书分类统计
     *
     * @return 各图书类型的借阅次数统计列表
     */
    @Override
    public R<List<BorrowTypeDTO>> getBorrowTypeStatistic() {
        HashMap<String, Integer> hashMap = new HashMap<>();
        List<BorrowTypeDTO> list = new ArrayList<>();

        List<BookBorrows> bookBorrowsList = booksBorrowService.list();
        for (BookBorrows bookBorrows : bookBorrowsList) {
            Long bookNumber = bookBorrows.getBookNumber();
            LambdaQueryWrapper<Books> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Books::getBookNumber, bookNumber);
            Books book = this.getOne(queryWrapper);
            if (book == null) {
                VueBookException.cast(CommonError.OBJECT_NULL);
            }
            String bookType = book.getBookType();
            hashMap.put(bookType, hashMap.getOrDefault(bookType, 0) + 1);
        }

        Set<Map.Entry<String, Integer>> entries = hashMap.entrySet();
        for (Map.Entry<String, Integer> entry : entries) {
            BorrowTypeDTO borrowTypeDTO = new BorrowTypeDTO();
            borrowTypeDTO.setBookTypes(entry.getKey());
            borrowTypeDTO.setBorrowNumbers(entry.getValue());
            list.add(borrowTypeDTO);
        }
        return R.success(list, "获取借书分类统计情况成功");
    }

    /**
     * 批量删除图书
     *
     * @param booksList 要删除的图书列表
     * @return 操作结果，成功返回"批量删除图书成功"
     */
    @Override
    public R<String> deleteBookByBatch(List<Books> booksList) {
        ArrayList<Integer> list = new ArrayList<>();
        for (Books books : booksList) {
            list.add(books.getBookId());
        }
        boolean delete = this.removeBatchByIds(list);
        if (delete) {
            return R.success(null, "批量删除图书成功");
        }
        return R.error("批量删除图书失败");
    }
}