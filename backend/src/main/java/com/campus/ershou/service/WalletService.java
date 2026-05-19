package com.campus.ershou.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.ershou.common.BusinessException;
import com.campus.ershou.entity.Wallet;
import com.campus.ershou.entity.WalletTransaction;
import com.campus.ershou.mapper.WalletMapper;
import com.campus.ershou.mapper.WalletTransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletService {
    @Autowired
    private WalletMapper walletMapper;
    @Autowired
    private WalletTransactionMapper transactionMapper;

    public Wallet getOrCreate(Long userId) {
        Wallet w = walletMapper.selectOne(new LambdaQueryWrapper<Wallet>().eq(Wallet::getUserId, userId));
        if (w == null) {
            w = new Wallet();
            w.setUserId(userId);
            w.setBalance(BigDecimal.ZERO);
            w.setFrozen(BigDecimal.ZERO);
            walletMapper.insert(w);
        }
        return w;
    }

    @Transactional
    public void recharge(Long userId, BigDecimal amount, String remark) {
        Wallet w = getOrCreate(userId);
        w.setBalance(w.getBalance().add(amount));
        walletMapper.updateById(w);
        log(userId, amount, "RECHARGE", null, remark);
    }

    @Transactional
    public void pay(Long userId, BigDecimal amount, Long orderId) {
        Wallet w = getOrCreate(userId);
        if (w.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("钱包余额不足");
        }
        w.setBalance(w.getBalance().subtract(amount));
        walletMapper.updateById(w);
        log(userId, amount.negate(), "PAY", orderId, "订单支付");
    }

    @Transactional
    public void refund(Long userId, BigDecimal amount, Long orderId) {
        Wallet w = getOrCreate(userId);
        w.setBalance(w.getBalance().add(amount));
        walletMapper.updateById(w);
        log(userId, amount, "REFUND", orderId, "订单退款");
    }

    @Transactional
    public void settleToSeller(Long sellerId, BigDecimal amount, Long orderId) {
        Wallet w = getOrCreate(sellerId);
        w.setBalance(w.getBalance().add(amount));
        walletMapper.updateById(w);
        log(sellerId, amount, "SETTLE", orderId, "货款结算");
    }

    private void log(Long userId, BigDecimal amount, String type, Long orderId, String remark) {
        WalletTransaction t = new WalletTransaction();
        t.setUserId(userId);
        t.setAmount(amount);
        t.setType(type);
        t.setRelatedOrderId(orderId);
        t.setRemark(remark);
        transactionMapper.insert(t);
    }
}
