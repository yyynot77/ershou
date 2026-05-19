package com.campus.ershou.controller;

import com.campus.ershou.common.Result;
import com.campus.ershou.dto.*;
import com.campus.ershou.service.AuthService;
import com.campus.ershou.util.CaptchaStore;
import com.campus.ershou.util.ImageCaptchaUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证模块 Controller（无需登录，WebConfig 白名单）
 * <p>
 * 前端页面：Login.vue、Register.vue、RegisterMerchant.vue
 * <p>
 * 登录调用链：
 * 用户提交表单 → login(dto) → AuthService.login
 * → 校验验证码/密码/审核状态 → JwtUtil 签发 token
 * → 前端 userStore.setLogin → sessionStorage 持久化
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired private AuthService authService;
    @Autowired private CaptchaStore captchaStore;

    /**
     * 图形验证码
     * 前端：页面 mounted 调用 getCaptcha()，展示 base64 图，保存 captchaKey 供登录/注册提交
     */
    @GetMapping("/captcha")
    public Result<Map<String, String>> captcha() {
        ImageCaptchaUtil.CaptchaResult captcha = ImageCaptchaUtil.generate(130, 48, 4);
        String key = captchaStore.save(captcha.code());
        Map<String, String> map = new HashMap<>();
        map.put("captchaKey", key);
        map.put("captchaImage", captcha.imageBase64());
        return Result.ok(map);
    }

    /**
     * 用户登录
     * 前端：Login.vue submit → api.login → 本接口
     */
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    /**
     * 普通用户注册（状态 PENDING，需管理员审核后才能登录）
     * 前端：Register.vue
     */
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody UserRegisterDTO dto) {
        authService.registerUser(dto);
        return Result.ok("注册成功，请等待管理员审核");
    }

    /**
     * 商家注册（同时写入 merchant_info）
     * 前端：RegisterMerchant.vue
     */
    @PostMapping("/register/merchant")
    public Result<?> registerMerchant(@Valid @RequestBody MerchantRegisterDTO dto) {
        authService.registerMerchant(dto);
        return Result.ok("商家注册成功，请等待管理员审核");
    }
}
