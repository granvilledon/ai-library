package com.book.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book.backend.common.BasePage;
import com.book.backend.common.R;
import com.book.backend.common.exception.CommonError;
import com.book.backend.common.exception.VueBookException;
import com.book.backend.constant.Constant;
import com.book.backend.mapper.UsersMapper;
import com.book.backend.pojo.Users;
import com.book.backend.pojo.dto.UsersDTO;
import com.book.backend.service.UsersService;
import com.book.backend.utils.JwtKit;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;

/**
 * 用户服务实现类
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users>
        implements UsersService {

    private static final String SALT = "granvilledon";

    @Resource
    private JwtKit jwtKit;

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息，密码字段会被置空
     */
    @Override
    public R<Users> getUserByUserId(Integer userId) {
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        R<Users> result = new R<>();
        if (userId == null) {
            VueBookException.cast(CommonError.USER_NULL);
        }
        queryWrapper.eq(Users::getUserId, userId);
        Users users = this.getOne(queryWrapper);
        if (users == null) {
            result.setStatus(404);
            result.setMsg("获取用户信息失败");
            return result;
        }
        users.setPassword("");
        result.setData(users);
        result.setStatus(200);
        result.setMsg("获取用户信息成功");
        return result;
    }

    /**
     * 更新用户密码
     *
     * @param users 用户信息（包含用户ID和新密码）
     * @return 操作结果，成功返回"更改密码成功"
     */
    @Override
    public R<String> updatePassword(Users users) {
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        Long userId = users.getUserId();
        queryWrapper.eq(userId != null, Users::getUserId, userId);
        String password = users.getPassword();
        Users userOne = this.getOne(queryWrapper);
        if (userOne == null) {
            return R.error("更改密码失败");
        }
        String saltPassword = SALT + password;
        String md5Password = DigestUtils.md5DigestAsHex(saltPassword.getBytes());
        userOne.setPassword(md5Password);
        boolean update = this.update(userOne, queryWrapper);
        if (!update) {
            return R.error("更改密码失败");
        }
        return R.success(null, "更改密码成功");
    }

    /**
     * 用户登录
     *
     * @param users 用户登录信息（包含用户名和密码）
     * @return 登录结果，成功返回包含token和用户ID的对象
     */
    @Override
    public R login(Users users) {
        R result = new R<>();
        if (StringUtils.isBlank(users.getUsername())) {
            result.setStatus(404);
            return R.error("用户名不存在");
        }
        LambdaUpdateWrapper<Users> userWrapper = new LambdaUpdateWrapper<>();
        userWrapper.eq(Users::getUsername, users.getUsername());
        Users user = this.getOne(userWrapper);
        if (user == null) {
            result.setStatus(404);
            return R.error("用户名不存在");
        }
        if (Constant.DISABLE.equals(user.getStatus())) {
            return R.error("账号已被禁止登录");
        }

        String password = DigestUtils.md5DigestAsHex((SALT + users.getPassword()).getBytes());
        if (!password.equals(user.getPassword())) {
            result.setStatus(404);
            return R.error("用户名或密码错误");
        }
        // 生成带角色信息的Token，普通用户角色为"user"
        String token = jwtKit.generateToken(user, "user");
        result.setStatus(200);
        result.add("token", token);
        result.setMsg("登录成功");
        result.add("id", user.getUserId());
        return result;
    }

    /**
     * 获取用户数据
     *
     * @param users 用户对象（包含用户ID）
     * @return 用户信息，密码字段会被置空
     */
    @Override
    public R<Users> getUserData(Users users) {
        R<Users> r = new R<>();
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Users::getUserId, users.getUserId());
        Users userOne = this.getOne(queryWrapper);
        if (userOne == null) {
            return R.error("用户不存在");
        }
        userOne.setPassword("");
        r.setData(userOne);
        r.setStatus(200);
        r.setMsg("获取用户数据成功");
        return r;
    }

    /**
     * 获取借阅证列表（分页）
     *
     * @param basePage 分页参数（支持按条件搜索）
     * @return 分页的用户列表
     */
    @Override
    public R<Page<Users>> getStatementList(BasePage basePage) {
        int pageSize = basePage.getPageSize();
        int pageNum = basePage.getPageNum();
        String condition = basePage.getCondition();
        String query = basePage.getQuery();
        R<Page<Users>> result = new R<>();
        Page<Users> pageInfo = new Page<>(pageNum, pageSize);
        if (StringUtils.isBlank(condition) || StringUtils.isBlank(query)) {
            Page<Users> page = this.page(pageInfo);
            if (page.getTotal() == 0) {
                return R.error("借阅证列表为空");
            }
            result.setStatus(200);
            result.setMsg("获取借阅证列表成功");
            result.setData(pageInfo);
            return result;
        }
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(condition, query);
        Page<Users> page = this.page(pageInfo, queryWrapper);
        if (page.getTotal() == 0) {
            return R.error("借阅证列表为空");
        }
        result.setStatus(200);
        result.setData(pageInfo);
        result.setMsg("获取借阅证列表成功");
        return result;
    }

    /**
     * 根据用户ID获取借阅证信息
     *
     * @param userId 用户ID
     * @return 用户DTO，包含用户状态描述
     */
    @Override
    public R<UsersDTO> getStatementByUserId(Integer userId) {
        Users users = this.getById(userId);
        if (users == null) {
            return R.error("获取用户信息失败");
        }
        R<UsersDTO> result = new R<>();
        Integer status = users.getStatus();
        UsersDTO usersDTO = new UsersDTO();
        BeanUtils.copyProperties(users, usersDTO);
        if (status.equals(Constant.AVAILABLE)) {
            usersDTO.setUserStatus("可用");
        } else {
            usersDTO.setUserStatus("禁用");
        }
        result.setData(usersDTO);
        result.setStatus(200);
        result.setMsg("获取用户信息成功");
        return result;
    }

    /**
     * 更新借阅证信息
     *
     * @param usersDTO 用户DTO（密码若长度<32则进行MD5加密）
     * @return 操作结果，成功返回"修改借阅证信息成功"
     */
    @Override
    public R<String> updateStatement(UsersDTO usersDTO) {
        Users users = new Users();
        BeanUtils.copyProperties(usersDTO, users, "password", "userStatus");
        String userStatus = usersDTO.getUserStatus();
        if (Constant.USERAVAILABLE.equals(userStatus)) {
            users.setStatus(1);
        } else {
            users.setStatus(0);
        }
        String password = usersDTO.getPassword();
        if (password.length() >= Constant.MD5PASSWORD) {
            users.setPassword(password);
        } else {
            String saltPassword = SALT + password;
            String md5Password = DigestUtils.md5DigestAsHex(saltPassword.getBytes());
            users.setPassword(md5Password);
        }

        boolean update = this.updateById(users);
        if (!update) {
            return R.error("修改借阅证信息失败");
        }
        return R.success(null, "修改借阅证信息成功");
    }

    /**
     * 删除借阅证
     *
     * @param userId 用户ID
     * @return 操作结果，成功返回"删除借阅证成功"
     */
    @Transactional
    @Override
    public R<String> deleteStatementByUserId(Integer userId) {
        Users users = this.getById(userId);
        if (users == null) {
            return R.error("删除借阅证失败");
        }
        boolean remove = this.removeById(userId);
        if (!remove) {
            return R.error("删除借阅证失败");
        }
        return R.success(null, "删除借阅证成功");
    }
}