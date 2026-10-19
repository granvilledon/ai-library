package com.book.backend.utils;


import com.book.backend.common.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Component
public class JwtKit {

    @Resource
    private JwtProperties jwtProperties;

    public JwtKit() {
    }

    public JwtKit(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /**
     * 生成Token
     *
     * @param user 用户对象
     * @param role 用户角色(user/bookadmin/admin)
     * @return JWT Token字符串
     */
    public <T> String generateToken(T user, String role) {
        // 创建JWT的载荷数据，用于存储用户相关信息
        Map<String, Object> claims = new HashMap<String, Object>(10);
        claims.put("username", user.toString());
        claims.put("createdate", new Date());
        claims.put("id", System.currentTimeMillis());
        claims.put("role", role);

        // 构建JWT Token，设置载荷、过期时间和签名算法
        return Jwts.builder().addClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecret())
                .compact();
    }

    /**
     * 校验并解析Token
     *
     * @param token JWT Token字符串
     * @return Claims载荷数据
     */
    public Claims parseJwtToken(String token) {
        Claims claims = null;
        claims = Jwts.parser().setSigningKey(jwtProperties.getSecret())
                .parseClaimsJws(token)
                .getBody();
        return claims;
    }
}