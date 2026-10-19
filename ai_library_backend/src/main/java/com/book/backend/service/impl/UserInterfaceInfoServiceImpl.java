package com.book.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book.backend.mapper.UserInterfaceInfoMapper;
import com.book.backend.pojo.UserInterfaceInfo;
import com.book.backend.service.UserInterfaceInfoService;
import org.springframework.stereotype.Service;

/**
 * 用户接口调用统计服务实现类
 */
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
        implements UserInterfaceInfoService {

}




