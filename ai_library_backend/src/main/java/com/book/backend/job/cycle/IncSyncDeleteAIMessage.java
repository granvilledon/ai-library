package com.book.backend.job.cycle;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.book.backend.common.exception.BusinessException;
import com.book.backend.common.exception.ErrorCode;
import com.book.backend.pojo.AiIntelligent;
import com.book.backend.pojo.UserInterfaceInfo;
import com.book.backend.service.AiIntelligentService;
import com.book.backend.service.UserInterfaceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * AI消息清理定时任务，删除无效AI回复并恢复用户调用次数
 */
@Component
@EnableScheduling
@Slf4j
public class IncSyncDeleteAIMessage {

    @Resource
    private AiIntelligentService aiIntelligentService;
    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;


    /**
     * 定时清理无效的AI回复记录并为用户增加调用次数（每天执行一次）
     */
    @Scheduled(fixedRate = 60 * 60 * 24 * 1000)
    public void run() {
        log.info("========== 开始执行每日定时任务 ==========");

        // 1. 清理无效的AI回复数据并恢复次数
        log.info("步骤1: 查询并清理无效的AI回复数据");
        LambdaQueryWrapper<AiIntelligent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNull(AiIntelligent::getAiResult).or().eq(AiIntelligent::getAiResult, "");
        List<AiIntelligent> list = aiIntelligentService.list(queryWrapper);

        if (!list.isEmpty()) {
            log.info("发现 {} 条无效AI回复记录，开始恢复用户调用次数", list.size());
            list.forEach(item -> {
                Long userId = item.getUserId();
                UserInterfaceInfo user = userInterfaceInfoService.getById(userId);
                synchronized (IncSyncDeleteAIMessage.class) {
                    if (user != null) {
                        user.setLeftNum(user.getLeftNum() + 1);
                        boolean save = userInterfaceInfoService.save(user);
                        if (!save) {
                            log.error("恢复用户 {} 调用次数失败", userId);
                            throw new BusinessException(ErrorCode.OPERATION_ERROR, "操作定时任务失败");
                        }
                    }
                }
            });

            log.info("开始删除无效的AI回复记录");
            try {
                boolean remove = aiIntelligentService.remove(queryWrapper);
                if (remove) {
                    log.info("成功删除 {} 条无效AI回复记录", list.size());
                } else {
                    log.warn("删除无效AI回复记录失败");
                }
            } catch (Exception e) {
                log.error("删除无效AI回复记录时发生异常", e);
                throw new RuntimeException(e);
            }
        } else {
            log.info("未发现无效的AI回复记录");
        }

        // 2. 给所有用户增加20次调用机会
        log.info("步骤2: 为所有用户增加20次调用机会");
        List<UserInterfaceInfo> allUsers = userInterfaceInfoService.list();
        log.info("共找到 {} 个用户", allUsers.size());

        int successCount = 0;
        int failCount = 0;

        for (UserInterfaceInfo user : allUsers) {
            synchronized (IncSyncDeleteAIMessage.class) {
                try {
                    user.setLeftNum(user.getLeftNum() + 20);
                    boolean update = userInterfaceInfoService.updateById(user);
                    if (update) {
                        successCount++;
                    } else {
                        failCount++;
                        log.error("为用户 {} 增加调用次数失败", user.getId());
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("处理用户 {} 时发生异常", user.getId(), e);
                }
            }
        }

        log.info("========== 定时任务执行完成 ==========");
        log.info("无效记录处理: 查询 {} 条，删除成功", list.size());
        log.info("用户次数增加: 总数 {}, 成功 {}, 失败 {}", allUsers.size(), successCount, failCount);
    }

}
