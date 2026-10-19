package com.book.backend.controller.bookadmin;

import com.book.backend.common.R;
import com.book.backend.pojo.BookAdmins;
import com.book.backend.service.BookAdminsService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 图书管理员登录控制器
 */
@RestController
@RequestMapping("/bookadmin")
public class BookAdminLoginController {
    @Resource
    private BookAdminsService bookAdminsService;


    /**
     * 图书管理员登录
     *
     * @param users 图书管理员信息
     * @return 登录结果(包含Token)
     */
    @PostMapping("/login")
    @ApiOperation("图书管理员登录")
    public R login(@RequestBody BookAdmins users) {
        return bookAdminsService.login(users);
    }

    @PostMapping("/getBookAdminInfo")
    @ApiOperation("获取图书管理员信息")
    public R<BookAdmins> getUserData(@RequestBody BookAdmins bookAdmins) {
        return bookAdminsService.getUserData(bookAdmins);
    }

}
