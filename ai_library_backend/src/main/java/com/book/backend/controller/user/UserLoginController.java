package com.book.backend.controller.user;

import com.book.backend.common.R;
import com.book.backend.pojo.Users;
import com.book.backend.service.UsersService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 普通用户登录控制器
 */
@RestController
@RequestMapping("/user")
public class UserLoginController {
    @Resource
    private UsersService usersService;

    /**
     * 用户登录
     *
     * @param users 用户信息
     * @return 登录结果(包含Token)
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public R login(@RequestBody Users users) {
        return usersService.login(users);
    }

    @PostMapping("/getUserInfo")
    @ApiOperation("获取用户信息")
    public R<Users> getUserData(@RequestBody Users users) {
        return usersService.getUserData(users);
    }
}
