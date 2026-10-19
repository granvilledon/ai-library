package com.book.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.book.backend.common.BasePage;
import com.book.backend.common.R;
import com.book.backend.pojo.Violations;
import com.book.backend.pojo.dto.BorrowData;
import com.book.backend.pojo.dto.ViolationsDTO;

/**
 * 违章记录服务接口
 */
public interface ViolationService extends IService<Violations> {

    /**
     * 查询违章记录(分页+条件查询)
     *
     * @param basePage 分页参数和查询条件
     * @return 违章记录分页数据
     */
    R<Page<ViolationsDTO>> getViolationListByPage(BasePage basePage);

    /**
     * 获取借阅量统计数据(按周)
     *
     * @return 借阅统计数据
     */
    R<BorrowData> getBorrowDate();
}
