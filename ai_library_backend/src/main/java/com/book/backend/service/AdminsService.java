package com.book.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.book.backend.common.R;
import com.book.backend.pojo.Admins;
import com.book.backend.pojo.dto.UsersDTO;
import org.springframework.web.multipart.MultipartFile;


/**
 * 系统管理员服务接口
 */
public interface AdminsService extends IService<Admins> {

    /**
     * 添加借阅证
     *
     * @param usersDTO 用户DTO
     * @return 操作结果
     */
    R<String> addRule(UsersDTO usersDTO);

    /**
     * 系统管理员登录
     *
     * @param admins 管理员信息
     * @return 登录结果(含Token)
     */
    R login(Admins admins);

    /**
     * 获取管理员数据
     *
     * @param admin 管理员信息
     * @return 管理员数据
     */
    R<Admins> getUserData(Admins admin);

    /**
     * Excel批量导入图书
     *
     * @param file Excel文件
     * @return 导入结果
     */
    R<String> upload(MultipartFile file);
}
