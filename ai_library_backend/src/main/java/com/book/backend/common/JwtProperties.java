package com.book.backend.common;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT配置类，从配置文件读取JWT相关参数
 */
@Component
@Data
public class JwtProperties {
    /**
     * JWT在请求头中的key名称
     */
    @Value("${jwt.tokenHeader}")
    private String tokenHeader;

    /**
     * JWT签名密钥
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * JWT令牌过期时间(秒)
     */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * JWT令牌前缀
     */
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    public JwtProperties() {
    }

    public JwtProperties(String tokenHeader, String secret, long expiration, String tokenHead) {
        this.tokenHeader = tokenHeader;
        this.secret = secret;
        this.expiration = expiration;
        this.tokenHead = tokenHead;
    }
}
