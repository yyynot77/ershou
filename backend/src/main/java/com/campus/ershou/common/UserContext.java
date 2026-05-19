package com.campus.ershou.common;

/**
 * 当前请求用户上下文（ThreadLocal）
 * <p>
 * 由 AuthInterceptor.preHandle 写入，afterCompletion 清理
 * <p>
 * Service 层通过 getUserId()/getRole() 获取登录用户，避免每层传参
 */
public class UserContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> ROLE = new ThreadLocal<>();

    public static void set(Long userId, String role) {
        USER_ID.set(userId);
        ROLE.set(role);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static String getRole() {
        return ROLE.get();
    }

    public static void clear() {
        USER_ID.remove();
        ROLE.remove();
    }
}
