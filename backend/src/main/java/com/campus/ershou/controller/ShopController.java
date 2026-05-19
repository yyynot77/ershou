package com.campus.ershou.controller;

import com.campus.ershou.common.Result;
import com.campus.ershou.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/shops")
public class ShopController {
    @Autowired private ProductService productService;

    @GetMapping("/{merchantId}")
    public Result<Map<String, Object>> shop(@PathVariable Long merchantId) {
        return Result.ok(productService.shopInfo(merchantId));
    }
}
