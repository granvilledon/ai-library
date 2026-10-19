package com.book.backend.manager;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 讯飞星火开放平台配置。
 *
 * <p>三项凭证不再写死在代码里，统一从配置文件 / 环境变量读取：
 * <pre>
 * spark:
 *   app-id: ${SPARK_APP_ID:}
 *   api-key: ${SPARK_API_KEY:}
 *   api-secret: ${SPARK_API_SECRET:}
 * </pre>
 *
 * <p>本地开发时把真实值写进 {@code application-local.yml}（该文件已被 .gitignore 排除），
 * 部署时用环境变量注入。详见仓库根目录 README 的「配置说明」。
 */
@Data
@Component
@ConfigurationProperties(prefix = "spark")
public class SparkProperties {

    /**
     * 应用 ID（讯飞开放平台控制台可见）
     */
    private String appId;

    /**
     * APIKey
     */
    private String apiKey;

    /**
     * APISecret，用于 HMAC-SHA256 签名
     */
    private String apiSecret;

    /**
     * 判断三项凭证是否都已配置，未配置时调用 AI 接口应直接返回友好提示而不是发一次注定失败的请求。
     */
    public boolean isConfigured() {
        return appId != null && !appId.isEmpty()
                && apiKey != null && !apiKey.isEmpty()
                && apiSecret != null && !apiSecret.isEmpty();
    }
}
