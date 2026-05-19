package com.campus.ershou.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.ershou.common.Result;
import com.campus.ershou.common.UserContext;
import com.campus.ershou.entity.*;
import com.campus.ershou.mapper.*;
import com.campus.ershou.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户中心 Controller（需登录）
 * <p>
 * 前端：Profile.vue（资料/流水）、Recharge.vue（自助充值）
 * <p>
 * 充值链：Recharge submit → selfRecharge → WalletService.recharge → wallet 表余额增加
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired private SysUserMapper userMapper;
    @Autowired private MerchantInfoMapper merchantInfoMapper;
    @Autowired private WalletService walletService;
    @Autowired private UserPointsMapper pointsMapper;
    @Autowired private WalletTransactionMapper transactionMapper;

    @GetMapping("/profile")
    public Result<Map<String, Object>> profile() {
        Long uid = UserContext.getUserId();
        SysUser u = userMapper.selectById(uid);
        u.setPassword(null);
        Map<String, Object> map = new HashMap<>();
        map.put("user", u);
        map.put("wallet", walletService.getOrCreate(uid));
        map.put("points", pointsMapper.selectOne(new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, uid)));
        if ("MERCHANT".equals(u.getRole())) {
            map.put("merchant", merchantInfoMapper.selectOne(
                    new LambdaQueryWrapper<MerchantInfo>().eq(MerchantInfo::getUserId, uid)));
        }
        return Result.ok(map);
    }

    @PutMapping("/profile")
    public Result<?> update(@RequestBody SysUser user) {
        user.setId(UserContext.getUserId());
        user.setPassword(null);
        user.setRole(null);
        user.setStatus(null);
        userMapper.updateById(user);
        return Result.ok();
    }

    @PostMapping("/wallet/recharge")
    public Result<?> selfRecharge(@RequestParam BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.fail("充值金额须大于0");
        }
        Long uid = UserContext.getUserId();
        SysUser u = userMapper.selectById(uid);
        if (u.getBankAccount() == null || u.getBankAccount().isBlank()) {
            return Result.fail("请先在个人中心填写并保存银行卡号");
        }
        walletService.recharge(uid, amount, "银行卡充值（模拟）→ " + maskBank(u.getBankAccount()));
        Map<String, Object> map = new HashMap<>();
        map.put("wallet", walletService.getOrCreate(uid));
        return Result.ok(map);
    }

    private static String maskBank(String account) {
        if (account == null || account.length() < 8) return "****";
        return account.substring(0, 4) + " **** **** " + account.substring(account.length() - 4);
    }

    @GetMapping("/wallet/transactions")
    public Result<?> transactions() {
        return Result.ok(transactionMapper.selectList(
                new LambdaQueryWrapper<WalletTransaction>()
                        .eq(WalletTransaction::getUserId, UserContext.getUserId())
                        .orderByDesc(WalletTransaction::getCreateTime)));
    }
}
