package com.campus.ershou.controller;

import com.campus.ershou.common.Result;
import com.campus.ershou.entity.Banner;
import com.campus.ershou.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
public class BannerController {
    @Autowired private AdminService adminService;

    @GetMapping
    public Result<List<Banner>> list() {
        return Result.ok(adminService.banners());
    }
}
