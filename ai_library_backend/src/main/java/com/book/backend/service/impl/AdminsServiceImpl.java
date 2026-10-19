package com.book.backend.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book.backend.common.R;
import com.book.backend.constant.Constant;
import com.book.backend.mapper.AdminsMapper;
import com.book.backend.pojo.Admins;
import com.book.backend.pojo.Books;
import com.book.backend.pojo.Users;
import com.book.backend.pojo.dto.BookData;
import com.book.backend.pojo.dto.UsersDTO;
import com.book.backend.service.AdminsService;
import com.book.backend.service.BooksService;
import com.book.backend.service.UsersService;
import com.book.backend.utils.JwtKit;
import com.book.backend.utils.NumberUtil;
import com.book.backend.utils.RandomNameUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;

/**
 * 系统管理员服务实现类
 */
@Service
public class AdminsServiceImpl extends ServiceImpl<AdminsMapper, Admins>
        implements AdminsService {

    /**
     * 密码加密盐值
     */
    private static final String SALT = "granvilledon";

    @Resource
    private UsersService usersService;

    @Resource
    private BooksService booksService;

    @Resource
    private JwtKit jwtKit;

    /**
     * 添加借阅证（创建用户）
     *
     * @param usersDTO 用户信息DTO
     * @return 操作结果，成功返回"添加借阅证成功"，失败返回错误信息
     */
    @Override
    public R<String> addRule(UsersDTO usersDTO) {
        String password = SALT + usersDTO.getPassword();
        String userStatus = usersDTO.getUserStatus();
        int status = 0;
        if (Constant.USERAVAILABLE.equals(userStatus)) {
            status = 1;
        }
        Users users = new Users();
        BeanUtils.copyProperties(usersDTO, users, "userStatus");
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        users.setStatus(status);
        users.setCardName(RandomNameUtils.generateNicknameWithNumber());
        users.setPassword(md5Password);
        long cardNumber = Long.parseLong(new String(NumberUtil.getNumber(11)));
        users.setCardNumber(cardNumber);
        boolean save = usersService.save(users);
        if (!save) {
            return R.error("添加借阅证失败");
        }
        return R.success(null, "添加借阅证成功");
    }

    /**
     * 管理员登录
     *
     * @param users 管理员登录信息（包含用户名和密码）
     * @return 登录结果，成功返回包含token和管理员ID的对象
     */
    @Override
    public R login(Admins users) {
        R result = new R<>();
        if (StringUtils.isBlank(users.getUsername())) {
            result.setStatus(404);
            return R.error("用户名不存在");
        }
        LambdaUpdateWrapper<Admins> adminWrapper = new LambdaUpdateWrapper<>();
        adminWrapper.eq(Admins::getUsername, users.getUsername());
        Admins adminOne = this.getOne(adminWrapper);
        if (adminOne == null) {
            result.setStatus(404);
            return R.error("用户名不存在");
        }
        if (Constant.DISABLE.equals(adminOne.getStatus())) {
            return R.error("该系统管理员已被禁用");
        }
        String password = DigestUtils.md5DigestAsHex((SALT + users.getPassword()).getBytes());
        if (!password.equals(adminOne.getPassword())) {
            result.setStatus(404);
            return R.error("用户名或密码错误");
        }
        // 生成带角色信息的Token，系统管理员角色为"admin"
        String token = jwtKit.generateToken(users, "admin");
        result.setStatus(200);
        result.add("token", token);
        result.setMsg("登录成功");
        result.add("id", adminOne.getAdminId());
        return result;
    }

    /**
     * 获取管理员信息
     *
     * @param admin 管理员对象（包含管理员ID）
     * @return 管理员信息，密码字段会被置空
     */
    @Override
    public R<Admins> getUserData(Admins admin) {
        R<Admins> r = new R<>();
        LambdaQueryWrapper<Admins> adminsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        adminsLambdaQueryWrapper.eq(Admins::getAdminId, admin.getAdminId());
        Admins adminOne = this.getOne(adminsLambdaQueryWrapper);
        if (adminOne == null) {
            return R.error("系统管理员不存在");
        }
        adminOne.setPassword("");
        r.setData(adminOne);
        r.setStatus(200);
        r.setMsg("获取系统管理员数据成功");
        return r;
    }

    /**
     * 批量导入图书（通过Excel文件）
     *
     * @param file Excel文件（.xls或.xlsx格式）
     * @return 操作结果
     */
    @Override
    public R<String> upload(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return R.error("文件不能为空");
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx"))) {
                return R.error("请上传Excel文件(.xls或.xlsx)");
            }

            final int[] successCount = {0};
            final boolean[] hasError = {false};

            EasyExcel.read(file.getInputStream(), BookData.class, new AnalysisEventListener<BookData>() {
                ArrayList<Books> booksList = new ArrayList<>();

                @Override
                public void invoke(BookData bookData, AnalysisContext analysisContext) {
                    try {
                        Books books = new Books();
                        BeanUtils.copyProperties(bookData, books);

                        if (bookData.getBookNumber() == null || bookData.getBookNumber() == 0) {
                            StringBuilder stringBuilder = NumberUtil.getNumber(11);
                            long bookNumber = Long.parseLong(new String(stringBuilder));
                            books.setBookNumber(bookNumber);
                        }

                        booksList.add(books);
                    } catch (Exception e) {
                        hasError[0] = true;
                        System.err.println("解析第" + (analysisContext.readRowHolder().getRowIndex() + 1) + "行数据失败: " + e.getMessage());
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                    if (!booksList.isEmpty()) {
                        try {
                            boolean flag = booksService.saveBatch(booksList);
                            if (flag) {
                                successCount[0] = booksList.size();
                                System.out.println("Excel批量添加图书成功，共" + successCount[0] + "条");
                            } else {
                                hasError[0] = true;
                                System.err.println("Excel批量添加图书失败");
                            }
                        } catch (Exception e) {
                            hasError[0] = true;
                            System.err.println("保存数据到数据库失败: " + e.getMessage());
                        }
                    } else {
                        System.out.println("空表无法进行数据导入");
                    }
                    System.out.println("解析完成...");
                }
            }).sheet().doRead();

            if (hasError[0]) {
                return R.error("部分数据导入失败，请检查文件格式");
            }

            return R.success(null, "Excel批量添加图书成功，共导入" + successCount[0] + "条记录");
        } catch (Exception e) {
            System.err.println("文件上传失败: " + e.getMessage());
            e.printStackTrace();
            return R.error("文件上传失败: " + e.getMessage());
        }
    }
}