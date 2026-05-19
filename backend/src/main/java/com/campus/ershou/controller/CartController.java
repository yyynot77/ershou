package com.campus.ershou.controller;

import com.campus.ershou.common.Result;
import com.campus.ershou.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 购物车 Controller
 * <p>
 * 需登录：AuthInterceptor 从 Token 注入 UserContext.getUserId()
 * <p>
 * 前端页面：Cart.vue、ProductCard.vue、ProductDetail.vue、stores/cart.js
 * <p>
 * 加购完整调用链：
 * 用户点击「加入购物车」→ 前端 addCart(productId, qty)
 * → POST /api/cart → CartService.add
 * → 校验商品 PUBLISHED → cart_item 表 insert/累加 quantity
 * → 前端 cartStore.refresh() → GET /api/cart 更新角标
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {
    @Autowired private CartService cartService;

    /**
     * 购物车列表（含商品、首图、店铺名）
     * 前端：Cart.vue load()、cartStore.refresh()
     */
    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(cartService.list());
    }

    /**
     * 加入购物车
     * 前端：ProductCard.quickAdd / ProductDetail.addToCart
     * @param productId 商品 ID
     * @param quantity 数量，默认 1
     */
    @PostMapping
    public Result<?> add(@RequestParam Long productId, @RequestParam(defaultValue = "1") Integer quantity) {
        cartService.add(productId, quantity);
        return Result.ok();
    }

    /**
     * 修改购物车项数量
     * 前端：Cart.vue el-input-number @change → updateQty → updateCart(id, q)
     * FIXME：与 add 接口职责分离，但前端需处理售罄时后端删除记录的情况
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestParam Integer quantity) {
        cartService.updateQty(id, quantity);
        return Result.ok();
    }

    /**
     * 删除购物车项
     * 前端：Cart.vue 删除按钮 → removeCart
     */
    @DeleteMapping("/{id}")
    public Result<?> remove(@PathVariable Long id) {
        cartService.remove(id);
        return Result.ok();
    }
}
