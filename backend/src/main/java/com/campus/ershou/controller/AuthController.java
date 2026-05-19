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

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired private AuthService authService;
    @Autowired private CaptchaStore captchaStore;

    @GetMapping("/captcha")
    public Result<Map<String, String>> captcha() {
        ImageCaptchaUtil.CaptchaResult captcha = ImageCaptchaUtil.generate(130, 48, 4);
        String key = captchaStore.save(captcha.code());
        Map<String, String> map = new HashMap<>();
        map.put("captchaKey", key);
        map.put("captchaImage", captcha.imageBase64());
        return Result.ok(map);
    }

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody UserRegisterDTO dto) {
        authService.registerUser(dto);
        return Result.ok("注册成功，请等待管理员审核");
    }

    @PostMapping("/register/merchant")
    public Result<?> registerMerchant(@Valid @RequestBody MerchantRegisterDTO dto) {
        authService.registerMerchant(dto);
        return Result.ok("商家注册成功，请等待管理员审核");
    }
}
