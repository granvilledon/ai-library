package com.book.backend.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.book.backend.common.JwtProperties;
import com.book.backend.constant.Constant;
import com.book.backend.utils.JwtKit;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * JWT认证拦截器，验证请求Token的有效性
 */
public class AuthInterceptorHandler implements HandlerInterceptor {
    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private JwtKit jwtKit;

    /**
     * 请求预处理，验证JWT Token
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param handler  处理器对象
     * @return true放行，false拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // 非方法处理器直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // Swagger控制器请求放行
        if (("springfox.documentation.swagger.web.ApiResourceController").equals(handlerMethod.getBean().getClass().getName())) {
            return true;
        }

        // OPTIONS预检请求放行
        if ((Constant.OPTIONS).equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        // 获取并提取JWT Token
        String jwtToken = request.getHeader(jwtProperties.getTokenHeader());
        String payloadToken = null;
        JSONObject jsonObject = new JSONObject();

        if (jwtToken != null) {
            payloadToken = jwtToken.substring(jwtProperties.getTokenHead().length() + 1);
        }

        // 验证并解析Token
        if (payloadToken != null && (!(Constant.NULL).equals(payloadToken))) {
            Claims claims = null;
            try {
                claims = jwtKit.parseJwtToken(payloadToken);
            } catch (Exception e) {
                // Token过期返回401
                jsonObject.put("status", 401);
                jsonObject.put("msg", "登录过期,请重新登录");
                String json1 = jsonObject.toJSONString();
                renderJson(response, json1);
                return false;
            }

            // 角色权限校验
            if (!checkRolePermission(request, claims)) {
                jsonObject.put("status", 403);
                jsonObject.put("msg", "无权限访问该资源");
                renderJson(response, jsonObject.toJSONString());
                return false;
            }

            return true;
        }

        // Token不存在返回非法登录
        jsonObject.put("status", 401);
        jsonObject.put("msg", "登录非法");
        String json2 = jsonObject.toJSONString();
        renderJson(response, json2);

        return false;
    }

    /**
     * 检查角色权限
     *
     * @param request HTTP请求
     * @param claims  Token载荷数据
     * @return true表示有权限，false表示无权限
     */
    private boolean checkRolePermission(HttpServletRequest request, Claims claims) {
        String requestURI = request.getRequestURI();
        String userRole = claims.get("role", String.class);

        // 如果Token中没有角色信息，拒绝访问
        if (userRole == null || userRole.isEmpty()) {
            return false;
        }

        // 管理员接口：仅admin角色可访问
        if (requestURI.startsWith("/api/admin") && !"admin".equals(userRole)) {
            return false;
        }

        // 图书管理员接口：admin和bookadmin角色可访问
        if (requestURI.startsWith("/api/bookadmin")) {
            if (!"admin".equals(userRole) && !"bookadmin".equals(userRole)) {
                return false;
            }
        }

        // 普通用户接口：所有角色都可访问
        return true;
    }

    /**
     * 向响应中写入JSON数据
     *
     * @param response HTTP响应
     * @param json     JSON字符串
     */
    private void renderJson(HttpServletResponse response, String json) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter printWriter = response.getWriter()) {
            printWriter.print(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}