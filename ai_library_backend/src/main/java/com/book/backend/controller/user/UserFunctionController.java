package com.book.backend.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.book.backend.common.BasePage;
import com.book.backend.common.R;
import com.book.backend.pojo.*;
import com.book.backend.pojo.dto.CommentDTO;
import com.book.backend.pojo.dto.ViolationsDTO;
import com.book.backend.service.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 普通用户功能控制器，提供图书查询、借阅、评论、AI推荐等功能
 */
@RestController
@RequestMapping("/user")
public class UserFunctionController {
    @Resource
    private BooksService booksService;
    @Resource
    private BookRuleService bookRuleService;
    @Resource
    private NoticeService noticeService;
    @Resource
    private UsersService usersService;
    @Resource
    private BooksBorrowService booksBorrowService;
    @Resource
    private ViolationService violationService;

    @Resource
    private CommentService commentService;
    @Resource
    private AiIntelligentService aiIntelligentService;

    /**
     * 图书分页查询(支持模糊搜索)
     *
     * @param basePage 分页参数和搜索条件
     * @return 分页图书列表
     */
    @PostMapping("/searchBookPage")
    @ApiOperation("图书查询")
    public R<Page<Books>> searchBookPage(@RequestBody BasePage basePage) {
        return booksService.searchBookPage(basePage);
    }

    /**
     * 查询读者规则列表
     *
     * @return 读者规则列表
     */
    @GetMapping("getRuleList")
    @ApiOperation("查询读者规则")
    public R<List<BookRules>> getRuleList() {
        return bookRuleService.getRuleList();
    }

    /**
     * 查询公告列表
     *
     * @return 公告列表
     */
    @GetMapping("getNoticeList")
    @ApiOperation("查询公告列表")
    public R<List<Notices>> getNoticeList() {
        return noticeService.getNoticeList();
    }

    /**
     * 根据用户ID查询个人信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("getUserInfo/{userId}")
    @ApiOperation("查询个人信息")
    public R<Users> getUserByUserId(@PathVariable("userId") Integer userId) {
        return usersService.getUserByUserId(userId);
    }

    /**
     * 修改密码
     *
     * @param users 用户信息(包含新密码)
     * @return 操作结果
     */
    @PostMapping("updatePassword")
    @ApiOperation("修改密码")
    public R<String> updatePassword(@RequestBody Users users) {
        return usersService.updatePassword(users);
    }

    /**
     * 查询个人借阅记录(分页+搜索)
     *
     * @param basePage 分页参数和搜索条件
     * @return 分页借阅记录
     */
    @PostMapping("getBorrowRecords")
    @ApiOperation("查询借阅记录")
    public R<Page<BookBorrows>> getBookBorrowPage(@RequestBody BasePage basePage) {
        return booksBorrowService.getBookBorrowPage(basePage);
    }

    /**
     * 查询违章记录(分页+搜索)
     *
     * @param basePage 分页参数和搜索条件
     * @return 分页违章记录
     */
    @PostMapping("getViolationRecords")
    @ApiOperation("查询违章记录")
    public R<Page<ViolationsDTO>> getViolationListByPage(@RequestBody BasePage basePage) {
        return violationService.getViolationListByPage(basePage);
    }

    /**
     * 获取弹幕列表
     *
     * @return 弹幕列表
     */
    @GetMapping("getCommentList")
    @ApiOperation("获取弹幕列表")
    public R<List<CommentDTO>> getCommentList() {
        return commentService.getCommentList();

    }

    /**
     * 发送弹幕
     *
     * @param commentDTO 弹幕信息
     * @return 操作结果
     */
    @PostMapping("addComment")
    @ApiOperation("发送弹幕")
    public R<String> addComment(@RequestBody CommentDTO commentDTO) {
        return commentService.addComment(commentDTO);
    }

    /**
     * AI智能推荐图书
     *
     * @param aiIntelligent AI推荐请求
     * @return 推荐结果
     */
    @PostMapping("aiRecommend")
    @ApiOperation("AI推荐图书")
    public R<String> aiRecommend(@RequestBody AiIntelligent aiIntelligent) {
        return aiIntelligentService.getGenResult(aiIntelligent);
    }

    /**
     * 查询用户与AI的最近5条聊天记录
     *
     * @param userId 用户ID
     * @return 聊天记录列表
     */
    @GetMapping("getAiChatHistory/{userId}")
    @ApiOperation("查询AI聊天记录")
    public R<List<AiIntelligent>> getAiInformationByUserId(@PathVariable("userId") Long userId) {
        return aiIntelligentService.getAiInformationByUserId(userId);
    }
}
