package com.campus.ershou.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.ershou.common.Result;
import com.campus.ershou.common.UserContext;
import com.campus.ershou.entity.*;
import com.campus.ershou.mapper.*;
import com.campus.ershou.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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

    @GetMapping("/wallet/transactions")
    public Result<?> transactions() {
        return Result.ok(transactionMapper.selectList(
                new LambdaQueryWrapper<WalletTransaction>()
                        .eq(WalletTransaction::getUserId, UserContext.getUserId())
                        .orderByDesc(WalletTransaction::getCreateTime)));
    }
}
