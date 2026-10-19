package com.book.backend.controller.admin;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.book.backend.common.BasePage;
import com.book.backend.common.R;
import com.book.backend.pojo.*;
import com.book.backend.pojo.dto.*;
import com.book.backend.pojo.dto.chart.GenChartByAiRequest;
import com.book.backend.pojo.vo.BiResponse;
import com.book.backend.service.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;


/**
 * 系统管理员功能控制器，提供图书管理、用户管理、统计分析等功能
 */
@RestController
@RequestMapping("admin")
public class AdminFunctionController {
    @Resource
    private BooksService booksService;
    @Resource
    private BookTypeService bookTypeService;
    @Resource
    private UsersService usersService;
    @Resource
    private BookRuleService bookRuleService;
    @Resource
    private BookAdminsService bookAdminsService;
    @Resource
    private ViolationService violationService;

    @Resource
    private AdminsService adminsService;
    @Resource
    private ChartService chartService;
    @Resource
    private AiIntelligentService aiIntelligentService;

    /**
     * 获取图书列表(分页+搜索)
     *
     * @param basePage 分页参数和搜索条件
     * @return 分页图书列表
     */
    @PostMapping("getBookList")
    @ApiOperation("获取图书列表")
    public R<Page<Books>> getBookList(@RequestBody BasePage basePage) {
        return booksService.getBookList(basePage);
    }

    /**
     * 查询书籍类型列表，用于添加图书时回显分类
     *
     * @return 书籍类型列表
     */
    @GetMapping("getTypeList")
    @ApiOperation("查询书籍类型列表")
    public R<List<BookTypes>> getBookTypeList() {
        return bookTypeService.getBookTypeList();
    }

    /**
     * 添加图书，通过DTO接收图书信息和类别ID
     *
     * @param bookDTO 图书信息DTO
     * @return 操作结果
     */
    @PostMapping("addBook")
    @ApiOperation("添加图书")
    public R<String> addBook(@RequestBody BookDTO bookDTO) {
        return booksService.addBook(bookDTO);
    }

    /**
     * 根据图书ID删除图书
     *
     * @param bookId 图书ID
     * @return 操作结果
     */
    @DeleteMapping("deleteBook/{bookId}")
    @ApiOperation("删除图书")
    public R<String> deleteBookByBookId(@PathVariable("bookId") Integer bookId) {
        return booksService.deleteBookByBookId(bookId);
    }

    /**
     * 根据图书ID查询图书信息
     *
     * @param bookId 图书ID
     * @return 图书信息
     */
    @GetMapping("getBookInfo/{bookId}")
    @ApiOperation("查询图书信息")
    public R<Books> getBookInformationByBookId(@PathVariable("bookId") Integer bookId) {
        return booksService.getBookInformationByBookId(bookId);
    }

    /**
     * 更新图书信息
     *
     * @param books 图书信息
     * @return 操作结果
     */
    @PutMapping("updateBook")
    @ApiOperation("更新图书")
    public R<String> updateBookByEditForm(@RequestBody Books books) {
        return booksService.updateBookByEditForm(books);
    }

    /**
     * 获取书籍类别列表(分页)
     *
     * @param basePage 分页参数和搜索条件
     * @return 分页书籍类别列表
     */
    @PostMapping("getBookTypePage")
    @ApiOperation("获取书籍类别列表")
    public R<Page<BookTypes>> getBookTypeListByPage(@RequestBody BasePage basePage) {
        return bookTypeService.getBookTypeListByPage(basePage);
    }

    /**
     * 添加书籍类别
     *
     * @param bookTypes 书籍类别
     * @return 操作结果
     */
    @PostMapping("addBookType")
    @ApiOperation("添加书籍类别")
    public R<String> addBookType(@RequestBody BookTypes bookTypes) {
        return bookTypeService.addBookType(bookTypes);
    }

    /**
     * 根据类别ID查询书籍类别信息
     *
     * @param typeId 类别ID
     * @return 书籍类别信息
     */
    @GetMapping("getBookType/{typeId}")
    @ApiOperation("查询书籍类别信息")
    public R<BookTypes> getBookTypeByTypeId(@PathVariable("typeId") Integer typeId) {
        return bookTypeService.getBookTypeByTypeId(typeId);
    }

    /**
     * 更新书籍类别
     *
     * @param bookTypes 书籍类别
     * @return 操作结果
     */
    @PutMapping("updateBookType")
    @ApiOperation("更新书籍类别")
    public R<String> updateBookType(@RequestBody BookTypes bookTypes) {
        return bookTypeService.updateBookType(bookTypes);
    }

    /**
     * 根据类别ID删除书籍类别
     *
     * @param typeId 类别ID
     * @return 操作结果
     */
    @DeleteMapping("deleteBookType/{typeId}")
    @ApiOperation("删除书籍类别")
    public R<String> deleteBookTypeByTypeId(@PathVariable("typeId") Integer typeId) {
        return bookTypeService.deleteBookTypeByTypeId(typeId);
    }

    /**
     * 获取借阅证列表(用户列表，分页+搜索)
     *
     * @param basePage 分页参数和搜索条件
     * @return 分页用户列表
     */
    @PostMapping("getStatementList")
    @ApiOperation("获取借阅证列表")
    public R<Page<Users>> getStatementList(@RequestBody BasePage basePage) {
        return usersService.getStatementList(basePage);
    }

    /**
     * 添加借阅证
     *
     * @param usersDTO 用户信息DTO
     * @return 操作结果
     */
    @PostMapping("addStatement")
    @ApiOperation("添加借阅证")
    public R<String> addStatement(@RequestBody UsersDTO usersDTO) {
        return adminsService.addRule(usersDTO);
    }

    /**
     * 根据用户ID查询用户信息，用于回显借阅证
     *
     * @param userId 用户ID
     * @return 用户信息DTO
     */
    @GetMapping("getStatement/{userId}")
    @ApiOperation("查询用户信息")
    public R<UsersDTO> getStatementByUserId(@PathVariable("userId") Integer userId) {
        return usersService.getStatementByUserId(userId);
    }

    /**
     * 修改借阅证信息
     *
     * @param usersDTO 用户信息DTO
     * @return 操作结果
     */
    @PutMapping("updateStatement")
    @ApiOperation("修改借阅证信息")
    public R<String> updateStatement(@RequestBody UsersDTO usersDTO) {
        return usersService.updateStatement(usersDTO);
    }

    /**
     * 根据用户ID删除借阅证
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("deleteStatement/{userId}")
    @ApiOperation("删除借阅证")
    public R<String> deleteStatementByUserId(@PathVariable("userId") Integer userId) {
        return usersService.deleteStatementByUserId(userId);
    }

    /**
     * 获取规则列表(分页)
     *
     * @param basePage 分页参数
     * @return 分页规则列表
     */
    @PostMapping("getRuleListPage")
    @ApiOperation("获取规则列表")
    public R<Page<BookRules>> getRuleListByPage(@RequestBody BasePage basePage) {
        return bookRuleService.getRuleListByPage(basePage);
    }

    /**
     * 添加规则
     *
     * @param bookRules 图书规则
     * @return 操作结果
     */
    @PostMapping("addRule")
    @ApiOperation("添加规则")
    public R<String> addRule(@RequestBody BookRules bookRules) {
        return bookRuleService.addRule(bookRules);
    }

    /**
     * 根据规则ID查询规则
     *
     * @param ruleId 规则ID
     * @return 规则信息DTO
     */
    @GetMapping("getRule/{ruleId}")
    @ApiOperation("查询规则")
    public R<BookRulesDTO> getRuleByRuleId(@PathVariable("ruleId") Integer ruleId) {
        return bookRuleService.getRuleByRuleId(ruleId);
    }

    /**
     * 修改规则
     *
     * @param bookRuleDTO 规则信息DTO
     * @return 操作结果
     */
    @PutMapping("updateRule")
    @ApiOperation("修改规则")
    public R<String> updateRule(@RequestBody BookRulesDTO bookRuleDTO) {
        return bookRuleService.updateRule(bookRuleDTO);
    }

    /**
     * 根据规则ID删除规则
     *
     * @param ruleId 规则ID
     * @return 操作结果
     */
    @DeleteMapping("deleteRule/{ruleId}")
    @ApiOperation("删除规则")
    public R<String> deleteRule(@PathVariable("ruleId") Integer ruleId) {
        return bookRuleService.deleteRule(ruleId);
    }

    /**
     * 获取图书管理员列表(分页)
     *
     * @param basePage 分页参数和搜索条件
     * @return 分页图书管理员列表
     */
    @PostMapping("getBookAdminList")
    @ApiOperation("获取图书管理员列表")
    public R<Page<BookAdmins>> getBookAdminListByPage(@RequestBody BasePage basePage) {
        return bookAdminsService.getBookAdminListByPage(basePage);
    }

    /**
     * 添加图书管理员
     *
     * @param bookAdmins 图书管理员
     * @return 操作结果
     */
    @PostMapping("addBookAdmin")
    @ApiOperation("添加图书管理员")
    public R<String> addBookAdmin(@RequestBody BookAdmins bookAdmins) {
        return bookAdminsService.addBookAdmin(bookAdmins);
    }

    /**
     * 根据ID查询图书管理员信息
     *
     * @param bookAdminId 图书管理员ID
     * @return 图书管理员信息
     */
    @GetMapping("getBookAdmin/{bookAdminId}")
    @ApiOperation("查询图书管理员信息")
    public R<BookAdmins> getBookAdminById(@PathVariable("bookAdminId") Integer bookAdminId) {
        return bookAdminsService.getBookAdminById(bookAdminId);
    }

    /**
     * 根据ID删除图书管理员
     *
     * @param bookAdminId 图书管理员ID
     * @return 操作结果
     */
    @DeleteMapping("deleteBookAdmin/{bookAdminId}")
    @ApiOperation("删除图书管理员")
    public R<String> deleteBookAdminById(@PathVariable("bookAdminId") Integer bookAdminId) {
        return bookAdminsService.deleteBookAdminById(bookAdminId);
    }

    /**
     * 修改图书管理员信息
     *
     * @param bookAdmins 图书管理员
     * @return 操作结果
     */
    @PutMapping("updateBookAdmin")
    @ApiOperation("修改图书管理员")
    public R<String> updateBookAdmin(@RequestBody BookAdmins bookAdmins) {
        return bookAdminsService.updateBookAdmin(bookAdmins);
    }

    /**
     * 获取借阅量统计数据
     *
     * @return 借阅量统计
     */
    @GetMapping("getBorrowData")
    @ApiOperation("获取借阅量统计")
    public R<BorrowData> getBorrowData() {
        return violationService.getBorrowDate();
    }

    /**
     * 获取借书分类统计数据
     *
     * @return 分类统计列表
     */
    @GetMapping("getBorrowTypeStatistics")
    @ApiOperation("获取借书分类统计")
    public R<List<BorrowTypeDTO>> getBorrowTypeStatistic() {
        return booksService.getBorrowTypeStatistic();
    }

    /**
     * 批量删除图书
     *
     * @param booksList 图书列表
     * @return 操作结果
     */
    @DeleteMapping("deleteBookBatch")
    @ApiOperation("批量删除图书")
    public R<String> deleteBookByBatch(@RequestBody List<Books> booksList) {
        return booksService.deleteBookByBatch(booksList);

    }

    /**
     * 从Excel批量导入图书
     *
     * @param file Excel文件
     * @return 操作结果
     */
    @PostMapping("/uploadBooks")
    @ApiOperation("批量导入图书")
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        return adminsService.upload(file);
    }

    /**
     * AI生成图表，根据用户上传的文件和分析需求
     *
     * @param multipartFile       上传的文件
     * @param genChartByAiRequest 分析需求(分析目标、图表类型、名称)
     * @return 图表分析结果
     */
    @PostMapping("/generateChart")
    @ApiOperation("AI生成图表")
    public R<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                      GenChartByAiRequest genChartByAiRequest) {
        return chartService.genChartByAi(multipartFile, genChartByAiRequest);
    }

    /**
     * AI智能分析，获取系统数据分析和决策建议
     *
     * @param aiIntelligent AI分析请求
     * @return 分析结果
     */
    @PostMapping("/aiIntelligentAnalysis")
    @ApiOperation("AI智能分析")
    public R<String> aiIntelligentAnalysis(@RequestBody AiIntelligent aiIntelligent) {
        return aiIntelligentService.getAdminGenResult(aiIntelligent);
    }

    /**
     * 查询管理员与AI的最近5条聊天记录
     *
     * @param adminId 管理员ID
     * @return 聊天记录列表
     */
    @GetMapping("/getAiChatHistory/{adminId}")
    @ApiOperation("查询AI聊天记录")
    public R<List<AiIntelligent>> getAiInformationByAdminId(@PathVariable("adminId") Long adminId) {
        return aiIntelligentService.getAiInformationByAdminId(adminId);
    }
}