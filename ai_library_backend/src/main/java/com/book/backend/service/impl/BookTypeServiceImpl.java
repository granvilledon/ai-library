package com.book.backend.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book.backend.common.BasePage;
import com.book.backend.common.R;
import com.book.backend.mapper.BookTypeMapper;
import com.book.backend.pojo.BookTypes;
import com.book.backend.service.BookTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 图书分类服务实现类
 */
@Service
public class BookTypeServiceImpl extends ServiceImpl<BookTypeMapper, BookTypes>
        implements BookTypeService {

    /**
     * 获取所有图书分类
     *
     * @return 图书分类列表
     */
    @Override
    public R<List<BookTypes>> getBookTypeList() {
        List<BookTypes> list = this.list();
        R<List<BookTypes>> result = new R<>();
        if (list.isEmpty()) {
            return R.error("获取书籍分类失败");
        }
        result.setData(list);
        result.setMsg("获取书籍分类成功");
        result.setStatus(200);
        return result;
    }

    /**
     * 获取图书分类列表（分页）
     *
     * @param basePage 分页参数
     * @return 分页的图书分类列表
     */
    @Override
    public R<Page<BookTypes>> getBookTypeListByPage(BasePage basePage) {
        int pageNum = basePage.getPageNum();
        int pageSize = basePage.getPageSize();
        Page<BookTypes> pageInfo = new Page<>(pageNum, pageSize);
        Page<BookTypes> page = this.page(pageInfo);
        if (page.getTotal() == 0) {
            return R.error("书籍分类列表为空");
        }
        R<Page<BookTypes>> result = new R<>();
        result.setData(pageInfo);
        result.setMsg("获取书籍分类列表成功");
        result.setStatus(200);
        return result;
    }

    /**
     * 添加图书分类
     *
     * @param bookTypes 图书分类信息
     * @return 操作结果，成功返回"添加书籍类型成功"
     */
    @Override
    public R<String> addBookType(BookTypes bookTypes) {
        boolean save = this.save(bookTypes);
        if (!save) {
            return R.error("添加书籍类别失败");
        }
        return R.success(null, "添加书籍类型成功");
    }

    /**
     * 根据ID获取图书分类
     *
     * @param typeId 分类ID
     * @return 图书分类信息
     */
    @Override
    public R<BookTypes> getBookTypeByTypeId(Integer typeId) {
        BookTypes type = this.getById(typeId);
        if (type == null) {
            return R.error("获取书籍类别失败");
        }
        R<BookTypes> result = new R<>();
        result.setData(type);
        result.setStatus(200);
        result.setMsg("获取书籍类别成功");
        return result;
    }

    /**
     * 更新图书分类
     *
     * @param bookTypes 图书分类信息（分类名称和描述不能为空）
     * @return 操作结果，成功返回"更新书籍类别成功"
     */
    @Override
    public R<String> updateBookType(BookTypes bookTypes) {
        String typeContent = bookTypes.getTypeContent();
        String typeName = bookTypes.getTypeName();
        if (StringUtils.isBlank(typeContent) || StringUtils.isBlank(typeName)) {
            return R.error("更新书籍类别失败");
        }
        boolean update = this.updateById(bookTypes);
        if (!update) {
            return R.error("更新书籍类别失败");
        }
        return R.success(null, "更新书籍类别成功");
    }

    /**
     * 删除图书分类
     *
     * @param typeId 分类ID
     * @return 操作结果，成功返回"删除书籍类别成功"
     */
    @Transactional
    @Override
    public R<String> deleteBookTypeByTypeId(Integer typeId) {
        BookTypes bookTypes = this.getById(typeId);
        if (bookTypes == null) {
            return R.error("删除书籍类别失败");
        }
        boolean remove = this.removeById(typeId);
        if (!remove) {
            return R.error("删除书籍类别失败");
        }
        return R.success(null, "删除书籍类别成功");
    }
}