package com.book.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book.backend.common.BasePage;
import com.book.backend.common.R;
import com.book.backend.constant.Constant;
import com.book.backend.mapper.BookAdminsMapper;
import com.book.backend.pojo.BookAdmins;
import com.book.backend.pojo.Violations;
import com.book.backend.pojo.dto.ViolationsDTO;
import com.book.backend.service.BookAdminsService;
import com.book.backend.service.ViolationService;
import com.book.backend.utils.JwtKit;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 图书管理员服务实现类
 */
@Service
public class BookAdminsServiceImpl extends ServiceImpl<BookAdminsMapper, BookAdmins>
        implements BookAdminsService {

    /**
     * 密码加密盐值
     */
    private static final String SALT = "granvilledon";

    @Resource
    private JwtKit jwtKit;

    private ViolationService violationService;

    @Autowired
    public BookAdminsServiceImpl(@Lazy ViolationService violationService) {
        this.violationService = violationService;
    }

    /**
     * 添加图书管理员
     *
     * @param bookAdmins 图书管理员信息（用户名、密码等）
     * @return 操作结果，成功返回"添加图书管理员成功"
     */
    @Override
    public R<String> addBookAdmin(BookAdmins bookAdmins) {
        String password = SALT + bookAdmins.getPassword();
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        bookAdmins.setPassword(md5Password);
        bookAdmins.setStatus(Constant.AVAILABLE);
        boolean save = this.save(bookAdmins);
        if (!save) {
            return R.error("添加图书管理员失败");
        }
        return R.success(null, "添加图书管理员成功");
    }

    /**
     * 图书管理员登录
     *
     * @param users 图书管理员登录信息（包含用户名和密码）
     * @return 登录结果，成功返回包含token和管理员ID的对象
     */
    @Override
    public R login(BookAdmins users) {
        R result = new R<>();
        if (StringUtils.isBlank(users.getUsername())) {
            result.setStatus(404);
            return R.error("用户名不存在");
        }
        LambdaUpdateWrapper<BookAdmins> adminWrapper = new LambdaUpdateWrapper<>();
        adminWrapper.eq(BookAdmins::getUsername, users.getUsername());
        BookAdmins bookAdminOne = this.getOne(adminWrapper);
        if (bookAdminOne == null) {
            result.setStatus(404);
            return R.error("用户名不存在");
        }
        if (Constant.DISABLE.equals(bookAdminOne.getStatus())) {
            return R.error("该图书管理员已被禁用");
        }
        String password = DigestUtils.md5DigestAsHex((SALT + users.getPassword()).getBytes());
        if (!password.equals(bookAdminOne.getPassword())) {
            result.setStatus(404);
            return R.error("用户名或密码错误");
        }
        // 生成带角色信息的Token，图书管理员角色为"bookadmin"
        String token = jwtKit.generateToken(users, "bookadmin");
        result.setStatus(200);
        result.add("token", token);
        result.setMsg("登录成功");
        result.add("id", bookAdminOne.getBookAdminId());
        return result;
    }

    /**
     * 获取图书管理员信息
     *
     * @param bookAdmins 图书管理员对象（包含管理员ID）
     * @return 图书管理员信息，密码字段会被置空
     */
    @Override
    public R<BookAdmins> getUserData(BookAdmins bookAdmins) {
        R<BookAdmins> r = new R<>();
        LambdaQueryWrapper<BookAdmins> adminsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        adminsLambdaQueryWrapper.eq(BookAdmins::getBookAdminId, bookAdmins.getBookAdminId());
        BookAdmins bookAdminOne = this.getOne(adminsLambdaQueryWrapper);
        if (bookAdminOne == null) {
            return R.error("图书管理员不存在");
        }
        bookAdminOne.setPassword("");
        r.setData(bookAdminOne);
        r.setStatus(200);
        r.setMsg("获取图书管理员数据成功");
        return r;
    }

    /**
     * 获取借书报表（已归还记录）
     *
     * @param basePage 分页参数（支持按条件搜索）
     * @return 分页的借阅记录DTO列表，包含图书管理员姓名
     */
    @Override
    public R<Page<ViolationsDTO>> getBorrowStatement(BasePage basePage) {
        int pageSize = basePage.getPageSize();
        int pageNum = basePage.getPageNum();
        String condition = basePage.getCondition();
        String query = basePage.getQuery();
        Page<Violations> pageInfo = new Page<>(pageNum, pageSize);
        Page<ViolationsDTO> dtoPage = new Page<>(pageNum, pageSize);
        R<Page<ViolationsDTO>> result = new R<>();

        if (StringUtils.isBlank(condition) || StringUtils.isBlank(query)) {
            LambdaQueryWrapper<Violations> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.isNotNull(Violations::getReturnDate).orderByAsc(Violations::getBorrowDate);
            Page<Violations> page = violationService.page(pageInfo, queryWrapper);
            if (page.getTotal() == 0) {
                return R.error("借书报表为空");
            }
            BeanUtils.copyProperties(pageInfo, dtoPage, "records");
            List<Violations> records = page.getRecords();
            List<ViolationsDTO> dtoList = records.stream().map(item -> {
                ViolationsDTO violationDTO = new ViolationsDTO();
                BeanUtils.copyProperties(item, violationDTO);
                Integer violationAdminId = item.getViolationAdminId();
                LambdaQueryWrapper<BookAdmins> queryWrapper1 = new LambdaQueryWrapper<>();
                queryWrapper1.eq(BookAdmins::getBookAdminId, violationAdminId);
                BookAdmins bookAdmins = this.getOne(queryWrapper1);
                if (bookAdmins != null) {
                    String username = bookAdmins.getUsername();
                    violationDTO.setViolationAdmin(username);
                }
                return violationDTO;
            }).collect(Collectors.toList());
            dtoPage.setRecords(dtoList);
            result.setData(dtoPage);
            result.setStatus(200);
            result.setMsg("获取借书报表信息成功");
            return result;
        }
        QueryWrapper<Violations> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(condition, query).isNotNull("return_date");
        Page<Violations> page = violationService.page(pageInfo, queryWrapper);
        if (page.getTotal() == 0) {
            return R.error("借书报表为空");
        }
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        List<Violations> records = pageInfo.getRecords();
        List<ViolationsDTO> dtoList = records.stream().map(item -> {
            ViolationsDTO violationDTO = new ViolationsDTO();
            BeanUtils.copyProperties(item, violationDTO);
            Integer violationAdminId = item.getViolationAdminId();
            LambdaQueryWrapper<BookAdmins> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(BookAdmins::getBookAdminId, violationAdminId);
            BookAdmins bookAdmins = this.getOne(queryWrapper1);
            if (bookAdmins != null) {
                String username = bookAdmins.getUsername();
                violationDTO.setViolationAdmin(username);
            }
            return violationDTO;
        }).collect(Collectors.toList());
        dtoPage.setRecords(dtoList);
        result.setData(dtoPage);
        result.setStatus(200);
        result.setMsg("获取借书报表信息成功");
        return result;
    }

    /**
     * 获取图书管理员列表（分页）
     *
     * @param basePage 分页参数
     * @return 分页的图书管理员列表
     */
    @Override
    public R<Page<BookAdmins>> getBookAdminListByPage(BasePage basePage) {
        int pageNum = basePage.getPageNum();
        int pageSize = basePage.getPageSize();
        Page<BookAdmins> pageInfo = new Page<>(pageNum, pageSize);
        Page<BookAdmins> page = this.page(pageInfo);
        if (page.getTotal() == 0) {
            return R.error("图书管理员列表为空");
        }
        return R.success(pageInfo, "获取图书管理员列表成功");
    }

    /**
     * 根据ID获取图书管理员信息
     *
     * @param bookAdminId 图书管理员ID
     * @return 图书管理员信息
     */
    @Override
    public R<BookAdmins> getBookAdminById(Integer bookAdminId) {
        BookAdmins bookAdmins = this.getById(bookAdminId);
        if (bookAdmins == null) {
            return R.error("获取图书管理员信息失败");
        }
        return R.success(bookAdmins, "获取图书管理员信息成功");
    }

    /**
     * 删除图书管理员
     *
     * @param bookAdminId 图书管理员ID
     * @return 操作结果，成功返回"删除图书管理员成功"
     */
    @Override
    public R<String> deleteBookAdminById(Integer bookAdminId) {
        BookAdmins bookAdmins = this.getById(bookAdminId);
        if (bookAdmins == null) {
            return R.error("删除图书管理员失败");
        }
        boolean remove = this.removeById(bookAdminId);
        if (!remove) {
            return R.error("删除图书管理员失败");
        }
        return R.success(null, "删除图书管理员成功");
    }

    /**
     * 更新图书管理员信息
     *
     * @param bookAdmins 图书管理员信息（密码若长度<32则进行MD5加密）
     * @return 操作结果，成功返回"修改图书管理员成功"
     */
    @Override
    public R<String> updateBookAdmin(BookAdmins bookAdmins) {
        String password = bookAdmins.getPassword();
        if (password.length() >= Constant.MD5PASSWORD) {
            bookAdmins.setPassword(password);
        } else {
            String md5Password = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
            bookAdmins.setPassword(md5Password);
        }
        boolean update = this.updateById(bookAdmins);
        if (!update) {
            return R.error("修改图书管理员失败");
        }
        return R.success(null, "修改图书管理员成功");
    }
}