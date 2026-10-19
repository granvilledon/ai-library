package com.book.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.book.backend.pojo.Users;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UsersMapper extends BaseMapper<Users> {

}



