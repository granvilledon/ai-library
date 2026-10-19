package com.book.backend.manager;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Guava限流器管理器，控制用户请求频率
 */
@Service
@Slf4j
public class GuavaRateLimiterManager {

    /**
     * 执行限流检查
     *
     * @param userId 用户ID
     * @return true-允许通过，false-被限流
     */
    public boolean doRateLimit(Long userId) {
        // 每秒允许的最大访问速率为1个许可
        RateLimiter rateLimiter = RateLimiter.create(1);
        if (rateLimiter.tryAcquire()) {
            log.info("用户id: " + userId + "请求一个令牌成功");
            return true;
        } else {
            log.info("用户id: " + userId + "请求一个令牌失败");
            return false;
        }
    }
}
