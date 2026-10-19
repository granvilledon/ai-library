package com.book.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book.backend.common.BasePage;
import com.book.backend.common.R;
import com.book.backend.mapper.ViolationMapper;
import com.book.backend.pojo.BookAdmins;
import com.book.backend.pojo.Violations;
import com.book.backend.pojo.dto.BorrowData;
import com.book.backend.pojo.dto.ViolationsDTO;
import com.book.backend.service.BookAdminsService;
import com.book.backend.service.ViolationService;
import com.book.backend.utils.BorrowDateUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 违章记录服务实现类
 */
@Service
public class ViolationServiceImpl extends ServiceImpl<ViolationMapper, Violations>
        implements ViolationService {

    private BookAdminsService bookAdminsService;

    @Autowired
    public ViolationServiceImpl(@Lazy BookAdminsService bookAdminsService) {
        this.bookAdminsService = bookAdminsService;
    }

    /**
     * 获取用户违章记录列表（分页）
     *
     * @param basePage 分页参数（包含cardNumber，支持按条件搜索）
     * @return 分页的违章记录DTO列表，包含处理管理员姓名
     */
    @Override
    public R<Page<ViolationsDTO>> getViolationListByPage(BasePage basePage) {
        String cardNumber = basePage.getCardNumber();
        QueryWrapper<Violations> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("card_number", cardNumber);
        List<Violations> list = this.list(queryWrapper);
        if (list.isEmpty()) {
            return R.error("没有该用户的违章信息");
        }

        int pageNum = basePage.getPageNum();
        int pageSize = basePage.getPageSize();
        Page<Violations> pageInfo = new Page<>(pageNum, pageSize);
        Page<ViolationsDTO> dtoPage = new Page<>(pageNum, pageSize);
        R<Page<ViolationsDTO>> result = new R<>();

        String condition = basePage.getCondition();
        String query = basePage.getQuery();
        if (StringUtils.isBlank(condition) || StringUtils.isBlank(query)) {
            LambdaQueryWrapper<Violations> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(Violations::getCardNumber, cardNumber).orderByAsc(Violations::getCreateTime);
            this.page(pageInfo, queryWrapper1);
            BeanUtils.copyProperties(pageInfo, dtoPage, "records");
            List<Violations> records = pageInfo.getRecords();
            List<ViolationsDTO> listDTO = records.stream().map((item) -> {
                ViolationsDTO violationDTO = new ViolationsDTO();
                BeanUtils.copyProperties(item, violationDTO);
                Integer violationAdminId = item.getViolationAdminId();
                LambdaQueryWrapper<BookAdmins> queryWrapper2 = new LambdaQueryWrapper<>();
                queryWrapper2.eq(BookAdmins::getBookAdminId, violationAdminId);
                BookAdmins bookAdmins = bookAdminsService.getOne(queryWrapper2);
                if (bookAdmins != null) {
                    String username = bookAdmins.getUsername();
                    violationDTO.setViolationAdmin(username);
                }
                return violationDTO;
            }).collect(Collectors.toList());
            dtoPage.setRecords(listDTO);
            result.setData(dtoPage);
            result.setMsg("获取违章记录成功");
            result.setStatus(200);
            return result;
        }

        queryWrapper.like(condition, query);
        Page<Violations> page = this.page(pageInfo, queryWrapper);
        if (page.getTotal() == 0) {
            return R.error("查询不到该用户的违章记录");
        }
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        List<Violations> records2 = pageInfo.getRecords();
        List<ViolationsDTO> violationDTOList = records2.stream().map((item) -> {
            ViolationsDTO violationDTO2 = new ViolationsDTO();
            BeanUtils.copyProperties(item, violationDTO2);
            Integer violationAdminId = item.getViolationAdminId();
            LambdaQueryWrapper<BookAdmins> queryWrapper3 = new LambdaQueryWrapper<>();
            queryWrapper3.eq(BookAdmins::getBookAdminId, violationAdminId);
            BookAdmins bookAdmins2 = bookAdminsService.getOne(queryWrapper3);
            if (bookAdmins2 != null) {
                String username = bookAdmins2.getUsername();
                violationDTO2.setViolationAdmin(username);
            }
            return violationDTO2;
        }).collect(Collectors.toList());
        dtoPage.setRecords(violationDTOList);
        result.setData(dtoPage);
        result.setMsg("获取违章信息成功");
        result.setStatus(200);
        return result;
    }

    /**
     * 获取近四周借阅量统计
     *
     * @return 借阅数据统计，包含日期数组和对应周的借阅数量
     */
    @Override
    public R<BorrowData> getBorrowDate() {
        LocalDateTime now = LocalDateTime.now();
        String[] dateArray = BorrowDateUtil.getDateArray(now);
        LocalDateTime time1 = now.minusWeeks(1);
        LocalDateTime time2 = now.minusWeeks(2);
        LocalDateTime time3 = now.minusWeeks(3);
        LocalDateTime time4 = now.minusWeeks(4);
        LambdaQueryWrapper<Violations> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.between(Violations::getBorrowDate, time1, now);
        LambdaQueryWrapper<Violations> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.between(Violations::getBorrowDate, time2, time1);
        LambdaQueryWrapper<Violations> queryWrapper3 = new LambdaQueryWrapper<>();
        queryWrapper3.between(Violations::getBorrowDate, time3, time2);
        LambdaQueryWrapper<Violations> queryWrapper4 = new LambdaQueryWrapper<>();
        queryWrapper4.between(Violations::getBorrowDate, time4, time3);
        List<Violations> list1 = this.list(queryWrapper1);
        List<Violations> list2 = this.list(queryWrapper2);
        List<Violations> list3 = this.list(queryWrapper3);
        List<Violations> list4 = this.list(queryWrapper4);
        Integer[] borrowNumbers = new Integer[4];
        borrowNumbers[3] = list1.size();
        borrowNumbers[1] = list2.size();
        borrowNumbers[2] = list3.size();
        borrowNumbers[0] = list4.size();
        BorrowData borrowData = new BorrowData(dateArray, borrowNumbers);
        return R.success(borrowData, "获取借阅量成功");
    }
}