package com.campus.ershou.interceptor;

import com.campus.ershou.common.UserContext;
import com.campus.ershou.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 登录鉴权拦截器
 * <p>
 * 注册位置：WebConfig.addInterceptors，拦截 /api/**，白名单见 excludePathPatterns。
 * <p>
 * 完整调用链：
 * 前端 request.js 附加 Authorization: Bearer {token}
 * → 本类 preHandle 解析 JWT
 * → UserContext.set(userId, role) 供 Controller/Service 通过 UserContext.getUserId() 使用
 * → 业务处理完毕 afterCompletion 清理 ThreadLocal，防止线程池复用脏数据
 * <p>
 * 前端对应：Login 成功后 token 存 sessionStorage；401 时 request 响应拦截器跳转登录页
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 请求进入 Controller 前执行
     *
     * @return false 时直接返回 401 JSON，不进入 Controller
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 浏览器 CORS 预检请求放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || token.isBlank()) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\"}");
            return false;
        }
        try {
            Claims claims = jwtUtil.parse(token);
            Long userId = claims.get("userId", Long.class);
            String role = claims.get("role", String.class);
            UserContext.set(userId, role);
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"登录已过期\"}");
            return false;
        }
    }

    /** 请求结束（含异常）后清理 ThreadLocal */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
