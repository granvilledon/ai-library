package com.book.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.book.backend.common.BasePage;
import com.book.backend.common.R;
import com.book.backend.pojo.Notices;

import java.util.List;

/**
 * 公告服务接口
 */
public interface NoticeService extends IService<Notices> {

    /**
     * 获取所有公告
     *
     * @return 公告列表
     */
    R<List<Notices>> getNoticeList();

    /**
     * 获取公告列表(分页)
     *
     * @param basePage 分页参数
     * @return 公告分页数据
     */
    R<Page<Notices>> getNoticeList(BasePage basePage);

    /**
     * 添加公告
     *
     * @param notices 公告信息
     * @return 操作结果
     */
    R<String> addNotice(Notices notices);

    /**
     * 删除公告
     *
     * @param noticeId 公告ID
     * @return 操作结果
     */
    R<String> deleteNoticeById(Integer noticeId);

    /**
     * 根据ID获取公告
     *
     * @param noticeId 公告ID
     * @return 公告信息
     */
    R<Notices> getNoticeByNoticeId(Integer noticeId);

    /**
     * 修改公告
     *
     * @param noticeId 公告ID
     * @param notices  公告信息
     * @return 操作结果
     */
    R<String> updateNoticeByNoticeId(Integer noticeId, Notices notices);
}
