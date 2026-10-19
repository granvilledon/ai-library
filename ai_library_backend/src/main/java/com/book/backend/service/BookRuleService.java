package com.book.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.book.backend.common.BasePage;
import com.book.backend.common.R;
import com.book.backend.pojo.BookRules;
import com.book.backend.pojo.dto.BookRulesDTO;

import java.util.List;

/**
 * 图书借阅规则服务接口
 */
public interface BookRuleService extends IService<BookRules> {

    /**
     * 获取所有借阅规则
     *
     * @return 规则列表
     */
    R<List<BookRules>> getRuleList();

    /**
     * 获取规则列表(分页)
     *
     * @param basePage 分页参数
     * @return 规则分页数据
     */
    R<Page<BookRules>> getRuleListByPage(BasePage basePage);

    /**
     * 添加规则
     *
     * @param bookRules 规则信息
     * @return 操作结果
     */
    R<String> addRule(BookRules bookRules);

    /**
     * 根据ID查询规则
     *
     * @param ruleId 规则ID
     * @return 规则信息
     */
    R<BookRulesDTO> getRuleByRuleId(Integer ruleId);

    /**
     * 修改规则
     *
     * @param bookRuleDTO 规则信息
     * @return 操作结果
     */
    R<String> updateRule(BookRulesDTO bookRuleDTO);

    /**
     * 删除规则
     *
     * @param ruleId 规则ID
     * @return 操作结果
     */
    R<String> deleteRule(Integer ruleId);
}
