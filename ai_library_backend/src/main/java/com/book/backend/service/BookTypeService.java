package com.book.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.book.backend.common.BasePage;
import com.book.backend.common.R;
import com.book.backend.pojo.BookTypes;

import java.util.List;

/**
 * 图书分类服务接口
 */
public interface BookTypeService extends IService<BookTypes> {

    /**
     * 获取所有图书分类(用于下拉选择)
     *
     * @return 分类列表
     */
    R<List<BookTypes>> getBookTypeList();

    /**
     * 获取分类列表(分页)
     *
     * @param basePage 分页参数
     * @return 分类分页数据
     */
    R<Page<BookTypes>> getBookTypeListByPage(BasePage basePage);

    /**
     * 添加分类
     *
     * @param bookTypes 分类信息
     * @return 操作结果
     */
    R<String> addBookType(BookTypes bookTypes);

    /**
     * 根据ID获取分类信息
     *
     * @param typeId 分类ID
     * @return 分类信息
     */
    R<BookTypes> getBookTypeByTypeId(Integer typeId);

    /**
     * 修改分类
     *
     * @param bookTypes 分类信息
     * @return 操作结果
     */
    R<String> updateBookType(BookTypes bookTypes);

    /**
     * 删除分类
     *
     * @param typeId 分类ID
     * @return 操作结果
     */
    R<String> deleteBookTypeByTypeId(Integer typeId);
}
