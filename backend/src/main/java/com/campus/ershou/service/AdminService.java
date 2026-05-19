package com.campus.ershou.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.ershou.common.*;
import com.campus.ershou.entity.*;
import com.campus.ershou.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理后台业务：用户/商品审核、充值、商家等级、封禁、拉黑、轮播
 * <p>
 * 所有方法开头 checkAdmin()，依赖 UserContext.role === ADMIN
 * <p>
 * 前端入口：views/admin/Dashboard.vue 各 Tab
 */
@Service
public class AdminService {
    @Autowired private SysUserMapper userMapper;
    @Autowired private MerchantInfoMapper merchantInfoMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private WalletService walletService;
    @Autowired private MerchantBanMapper banMapper;
    @Autowired private BuyerBlacklistMapper blacklistMapper;
    @Autowired private BannerMapper bannerMapper;

    public void checkAdmin() {
        if (!Constants.ROLE_ADMIN.equals(UserContext.getRole())) {
            throw new BusinessException("需要管理员权限");
        }
    }

    public List<SysUser> pendingUsers() {
        checkAdmin();
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStatus, Constants.STATUS_PENDING));
    }

    public void auditUser(Long id, boolean pass) {
        checkAdmin();
        SysUser u = userMapper.selectById(id);
        if (u == null) throw new BusinessException("用户不存在");
        u.setStatus(pass ? Constants.STATUS_APPROVED : Constants.STATUS_REJECTED);
        userMapper.updateById(u);
    }

    public List<SysUser> allUsers() {
        checkAdmin();
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>().orderByDesc(SysUser::getCreateTime));
    }

    public void updateUser(SysUser user) {
        checkAdmin();
        user.setPassword(null);
        userMapper.updateById(user);
    }

    public void deleteUser(Long id) {
        checkAdmin();
        userMapper.deleteById(id);
    }

    public List<Product> pendingProducts() {
        checkAdmin();
        return productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, Constants.PRODUCT_PENDING));
    }

    public void setMerchantLevel(Long merchantUserId, int level) {
        checkAdmin();
        MerchantInfo mi = merchantInfoMapper.selectOne(
                new LambdaQueryWrapper<MerchantInfo>().eq(MerchantInfo::getUserId, merchantUserId));
        if (mi == null) throw new BusinessException("商家不存在");
        mi.setMerchantLevel(Math.min(Math.max(level, 1), 5));
        merchantInfoMapper.updateById(mi);
    }

    public void rechargeWallet(Long userId, BigDecimal amount) {
        checkAdmin();
        walletService.recharge(userId, amount, "管理员充值");
    }

    public void banMerchant(Long merchantId, String banType, String reason, LocalDateTime endTime) {
        checkAdmin();
        if (merchantId == null) throw new BusinessException("商家ID不能为空");
        MerchantBan ban = new MerchantBan();
        ban.setMerchantId(merchantId);
        ban.setBanType(banType);
        ban.setReason(reason);
        ban.setEndTime(endTime);
        banMapper.insert(ban);
        if ("SHOP_CLOSE".equals(banType)) {
            List<Product> products = productMapper.selectList(
                    new LambdaQueryWrapper<Product>().eq(Product::getMerchantId, merchantId)
                            .eq(Product::getStatus, Constants.PRODUCT_PUBLISHED));
            for (Product p : products) {
                p.setStatus(Constants.PRODUCT_OFF_SHELF);
                productMapper.updateById(p);
            }
        }
    }

    public void blacklistBuyer(Long buyerId, Long merchantId, String reason) {
        checkAdmin();
        BuyerBlacklist bl = new BuyerBlacklist();
        bl.setBuyerId(buyerId);
        bl.setMerchantId(merchantId);
        bl.setReason(reason);
        blacklistMapper.insert(bl);
    }

    /** 生效中的封禁（未过期） */
    public List<MerchantBan> listActiveMerchantBans() {
        checkAdmin();
        LocalDateTime now = LocalDateTime.now();
        return banMapper.selectList(new LambdaQueryWrapper<MerchantBan>()
                .and(w -> w.isNull(MerchantBan::getEndTime).or().gt(MerchantBan::getEndTime, now))
                .orderByDesc(MerchantBan::getCreateTime));
    }

    public void liftMerchantBan(Long banId) {
        checkAdmin();
        MerchantBan ban = banMapper.selectById(banId);
        if (ban == null) throw new BusinessException("封禁记录不存在");
        banMapper.deleteById(banId);
    }

    public List<BuyerBlacklist> listBuyerBlacklist() {
        checkAdmin();
        return blacklistMapper.selectList(new LambdaQueryWrapper<BuyerBlacklist>()
                .orderByDesc(BuyerBlacklist::getCreateTime));
    }

    public void removeBuyerBlacklist(Long id) {
        checkAdmin();
        if (blacklistMapper.selectById(id) == null) {
            throw new BusinessException("拉黑记录不存在");
        }
        blacklistMapper.deleteById(id);
    }

    public List<Banner> banners() {
        return bannerMapper.selectList(new LambdaQueryWrapper<Banner>()
                .eq(Banner::getEnabled, 1).orderByAsc(Banner::getSortOrder));
    }

    @Transactional
    public void saveBanner(Banner banner) {
        checkAdmin();
        if (banner.getId() == null) bannerMapper.insert(banner);
        else bannerMapper.updateById(banner);
    }
}
