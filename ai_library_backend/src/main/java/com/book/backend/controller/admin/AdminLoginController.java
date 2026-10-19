package com.book.backend.controller.admin;

import com.book.backend.common.R;
import com.book.backend.pojo.Admins;
import com.book.backend.service.AdminsService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 系统管理员登录控制器
 */
@RestController
@RequestMapping("/admin")
public class AdminLoginController {
    @Resource
    private AdminsService adminsService;

    /**
     * 系统管理员登录
     *
     * @param users 管理员信息
     * @return 登录结果(包含Token)
     */
    @PostMapping("/login")
    @ApiOperation("管理员登录")
    public R login(@RequestBody Admins users) {
        return adminsService.login(users);
    }

    @PostMapping("/getAdminInfo")
    @ApiOperation("获取管理员信息")
    public R<Admins> getUserData(@RequestBody Admins admin) {
        return adminsService.getUserData(admin);
    }
}
