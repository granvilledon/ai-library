package com.book.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book.backend.common.BasePage;
import com.book.backend.common.R;
import com.book.backend.mapper.NoticeMapper;
import com.book.backend.pojo.Notices;
import com.book.backend.service.NoticeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 公告服务实现类
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notices>
        implements NoticeService {

    /**
     * 获取公告列表（不分页）
     *
     * @return 公告列表，按创建时间升序排列
     */
    @Override
    public R<List<Notices>> getNoticeList() {
        LambdaQueryWrapper<Notices> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Notices::getCreateTime);
        List<Notices> list = this.list(queryWrapper);
        R<List<Notices>> result = new R<>();
        if (list.isEmpty()) {
            result.setStatus(404);
            result.setMsg("获取公告信息失败");
            return result;
        }
        result.setData(list);
        result.setStatus(200);
        result.setMsg("获取公告信息成功");
        return result;
    }

    /**
     * 获取公告列表（分页）
     *
     * @param basePage 分页参数
     * @return 分页的公告列表，按创建时间升序排列
     */
    @Override
    public R<Page<Notices>> getNoticeList(BasePage basePage) {
        int pageNum = basePage.getPageNum();
        int pageSize = basePage.getPageSize();
        Page<Notices> pageInfo = new Page<>(pageNum, pageSize);
        R<Page<Notices>> result = new R<>();
        LambdaQueryWrapper<Notices> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Notices::getCreateTime);
        Page<Notices> page = this.page(pageInfo, queryWrapper);
        if (page.getTotal() == 0) {
            return R.error("公告列表为空");
        }
        result.setStatus(200);
        result.setMsg("获取公告列表成功");
        result.setData(pageInfo);
        return result;
    }

    /**
     * 添加公告
     *
     * @param notices 公告信息（标题和内容不能为空）
     * @return 操作结果，成功返回"新增公告成功"
     */
    @Override
    public R<String> addNotice(Notices notices) {
        String noticeTitle = notices.getNoticeTitle();
        String noticeContent = notices.getNoticeContent();
        if (StringUtils.isBlank(noticeTitle) || StringUtils.isBlank(noticeContent)) {
            return R.error("新增公告失败，存在空值");
        }
        Notices notices1 = new Notices();
        notices1.setNoticeId(null);
        notices1.setNoticeTitle(noticeTitle);
        notices1.setNoticeContent(noticeContent);
        notices1.setNoticeAdminId(notices.getNoticeAdminId());
        boolean save = this.save(notices1);
        if (!save) {
            return R.error("新增公告失败");
        }
        return R.success(null, "新增公告成功");
    }

    /**
     * 删除公告
     *
     * @param noticeId 公告ID
     * @return 操作结果，成功返回"删除公告成功"
     */
    @Override
    public R<String> deleteNoticeById(Integer noticeId) {
        boolean remove = this.removeById(noticeId);
        if (!remove) {
            return R.error("删除公告失败");
        }
        return R.success(null, "删除公告成功");
    }

    /**
     * 根据ID获取公告
     *
     * @param noticeId 公告ID
     * @return 公告信息
     */
    @Override
    public R<Notices> getNoticeByNoticeId(Integer noticeId) {
        Notices notices = this.getById(noticeId);
        if (notices == null) {
            return R.error("获取公告失败");
        }
        R<Notices> result = new R<>();
        result.setStatus(200);
        result.setMsg("获取公告信息成功");
        result.setData(notices);
        return result;
    }

    /**
     * 更新公告
     *
     * @param noticeId 公告ID
     * @param notices  公告信息（标题和内容不能为空）
     * @return 操作结果，成功返回"修改公告成功"
     */
    @Override
    public R<String> updateNoticeByNoticeId(Integer noticeId, Notices notices) {
        String noticeTitle = notices.getNoticeTitle();
        String noticeContent = notices.getNoticeContent();
        if (StringUtils.isBlank(noticeTitle) || StringUtils.isBlank(noticeContent)) {
            return R.error("修改公告失败");
        }
        Notices notices2 = this.getById(noticeId);
        if (notices2 == null) {
            return R.error("修改公告失败");
        }
        notices2.setNoticeTitle(noticeTitle);
        notices2.setNoticeContent(noticeContent);
        boolean update = this.updateById(notices2);
        if (!update) {
            return R.error("修改公告失败");
        }

        return R.success(null, "修改公告成功");
    }
}