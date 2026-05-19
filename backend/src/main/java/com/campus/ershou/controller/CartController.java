package com.campus.ershou.controller;

import com.campus.ershou.common.Result;
import com.campus.ershou.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    @Autowired private CartService cartService;

    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(cartService.list());
    }

    @PostMapping
    public Result<?> add(@RequestParam Long productId, @RequestParam(defaultValue = "1") Integer quantity) {
        cartService.add(productId, quantity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestParam Integer quantity) {
        cartService.updateQty(id, quantity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> remove(@PathVariable Long id) {
        cartService.remove(id);
        return Result.ok();
    }
}
