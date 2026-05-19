package com.campus.ershou.controller;

import com.campus.ershou.common.Result;
import com.campus.ershou.entity.*;
import com.campus.ershou.service.AdminService;
import com.campus.ershou.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理后台 API（仅 ADMIN 角色应在前端路由限制，后端靠各 Service.checkAdmin）
 * <p>
 * 前端：views/admin/Dashboard.vue
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired private AdminService adminService;
    @Autowired private ProductService productService;

    @GetMapping("/users/pending")
    public Result<List<SysUser>> pendingUsers() {
        return Result.ok(adminService.pendingUsers());
    }

    @PostMapping("/users/{id}/audit")
    public Result<?> auditUser(@PathVariable Long id, @RequestParam boolean pass) {
        adminService.auditUser(id, pass);
        return Result.ok();
    }

    @GetMapping("/users")
    public Result<List<SysUser>> users() {
        List<SysUser> list = adminService.allUsers();
        list.forEach(u -> u.setPassword(null));
        return Result.ok(list);
    }

    @PutMapping("/users/{id}")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        adminService.updateUser(user);
        return Result.ok();
    }

    @DeleteMapping("/users/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return Result.ok();
    }

    @GetMapping("/products/pending")
    public Result<List<Product>> pendingProducts() {
        return Result.ok(adminService.pendingProducts());
    }

    @PostMapping("/products/{id}/audit")
    public Result<?> auditProduct(@PathVariable Long id, @RequestParam boolean pass) {
        productService.audit(id, pass);
        return Result.ok();
    }

    @PostMapping("/merchants/{id}/level")
    public Result<?> setLevel(@PathVariable Long id, @RequestParam int level) {
        adminService.setMerchantLevel(id, level);
        return Result.ok();
    }

    @PostMapping("/wallet/recharge")
    public Result<?> recharge(@RequestParam Long userId, @RequestParam BigDecimal amount) {
        adminService.rechargeWallet(userId, amount);
        return Result.ok();
    }

    @PostMapping("/merchants/{id}/ban")
    public Result<?> ban(@PathVariable Long id, @RequestParam String banType,
                         @RequestParam String reason,
                         @RequestParam(required = false) String endTime) {
        LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime) : null;
        adminService.banMerchant(id, banType, reason, end);
        return Result.ok();
    }

    @PostMapping("/blacklist")
    public Result<?> blacklist(@RequestParam Long buyerId,
                               @RequestParam(required = false) Long merchantId,
                               @RequestParam String reason) {
        adminService.blacklistBuyer(buyerId, merchantId, reason);
        return Result.ok();
    }

    @GetMapping("/merchant-bans")
    public Result<List<MerchantBan>> listMerchantBans() {
        return Result.ok(adminService.listActiveMerchantBans());
    }

    @DeleteMapping("/merchant-bans/{id}")
    public Result<?> liftMerchantBan(@PathVariable Long id) {
        adminService.liftMerchantBan(id);
        return Result.ok();
    }

    @GetMapping("/blacklist")
    public Result<List<BuyerBlacklist>> listBuyerBlacklist() {
        return Result.ok(adminService.listBuyerBlacklist());
    }

    @DeleteMapping("/blacklist/{id}")
    public Result<?> removeBuyerBlacklist(@PathVariable Long id) {
        adminService.removeBuyerBlacklist(id);
        return Result.ok();
    }

    @GetMapping("/banners")
    public Result<List<Banner>> banners() {
        return Result.ok(adminService.banners());
    }

    @PostMapping("/banners")
    public Result<?> saveBanner(@RequestBody Banner banner) {
        adminService.saveBanner(banner);
        return Result.ok();
    }
}
