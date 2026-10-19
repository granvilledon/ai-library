package com.book.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book.backend.common.BasePage;
import com.book.backend.common.R;
import com.book.backend.mapper.BookRuleMapper;
import com.book.backend.pojo.BookRules;
import com.book.backend.pojo.dto.BookRulesDTO;
import com.book.backend.service.BookRuleService;
import com.book.backend.utils.NumberUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 图书借阅规则服务实现类
 */
@Service
public class BookRuleServiceImpl extends ServiceImpl<BookRuleMapper, BookRules>
        implements BookRuleService {

    /**
     * 获取所有借阅规则
     *
     * @return 借阅规则列表
     */
    @Override
    public R<List<BookRules>> getRuleList() {
        List<BookRules> list = this.list();
        R<List<BookRules>> result = new R<>();
        if (list.isEmpty()) {
            result.setStatus(404);
            result.setMsg("获取读者规则失败");
            return result;
        }
        result.setData(list);
        result.setStatus(200);
        result.setMsg("获取读者规则成功");
        return result;
    }

    /**
     * 获取借阅规则列表（分页）
     *
     * @param basePage 分页参数
     * @return 分页的借阅规则列表，按创建时间升序排列
     */
    @Override
    public R<Page<BookRules>> getRuleListByPage(BasePage basePage) {
        int pageNum = basePage.getPageNum();
        int pageSize = basePage.getPageSize();
        Page<BookRules> pageInfo = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BookRules> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(BookRules::getCreateTime);
        Page<BookRules> page = this.page(pageInfo, queryWrapper);
        if (page.getTotal() == 0) {
            return R.error("规则列表为空");
        }
        R<Page<BookRules>> result = new R<>();
        result.setData(pageInfo);
        result.setMsg("获取规则列表成功");
        result.setStatus(200);
        return result;
    }

    /**
     * 添加借阅规则
     *
     * @param bookRules 借阅规则信息（规则ID会自动生成）
     * @return 操作结果，成功返回"添加图书规则成功"
     */
    @Override
    public R<String> addRule(BookRules bookRules) {
        bookRules.setBookRuleId(Integer.parseInt(new String(NumberUtil.getNumber(3))));
        boolean save = this.save(bookRules);
        if (!save) {
            return R.error("添加图书规则失败");
        }
        return R.success(null, "添加图书规则成功");
    }

    /**
     * 根据ID获取借阅规则
     *
     * @param ruleId 规则ID
     * @return 借阅规则DTO，包含解析后的可借阅图书馆列表
     */
    @Override
    public R<BookRulesDTO> getRuleByRuleId(Integer ruleId) {
        BookRules bookRules = this.getById(ruleId);
        if (bookRules == null) {
            return R.error("获取规则信息失败");
        }
        BookRulesDTO bookRuleDTO = new BookRulesDTO();
        BeanUtils.copyProperties(bookRules, bookRuleDTO, "bookLimitLibrary");
        String bookLimitLibrary = bookRules.getBookLimitLibrary();
        String[] split = bookLimitLibrary.split(",");
        bookRuleDTO.setCheckList(split);
        R<BookRulesDTO> result = new R<>();
        result.setData(bookRuleDTO);
        result.setStatus(200);
        result.setMsg("获取规则信息成功");
        return result;
    }

    /**
     * 更新借阅规则
     *
     * @param bookRuleDTO 借阅规则DTO（checkList会合并为逗号分隔的字符串）
     * @return 操作结果，成功返回"更新规则成功"
     */
    @Transactional
    @Override
    public R<String> updateRule(BookRulesDTO bookRuleDTO) {
        String[] checkList = bookRuleDTO.getCheckList();
        String bookLimitLibrary = String.join(",", checkList);
        BookRules bookRules = new BookRules();
        BeanUtils.copyProperties(bookRuleDTO, bookRules, "bookLimitLibrary");
        bookRules.setBookLimitLibrary(bookLimitLibrary);
        boolean update = this.updateById(bookRules);
        if (!update) {
            return R.error("更新规则失败");
        }
        return R.success(null, "更新规则成功");
    }

    /**
     * 删除借阅规则
     *
     * @param ruleId 规则ID
     * @return 操作结果，成功返回"删除规则成功"
     */
    @Transactional
    @Override
    public R<String> deleteRule(Integer ruleId) {
        BookRules bookRules = this.getById(ruleId);
        if (bookRules == null) {
            return R.error("删除规则失败");
        }
        boolean remove = this.removeById(ruleId);
        if (!remove) {
            return R.error("删除规则失败");
        }
        return R.success(null, "删除规则成功");
    }
}