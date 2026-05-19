package com.campus.ershou.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.ershou.common.BusinessException;
import com.campus.ershou.entity.*;
import com.campus.ershou.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ReviewService {
    @Autowired private ProductReviewMapper productReviewMapper;
    @Autowired private BuyerMerchantReviewMapper buyerMerchantReviewMapper;
    @Autowired private MerchantBuyerReviewMapper merchantBuyerReviewMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private MerchantInfoMapper merchantInfoMapper;
    @Autowired private OrdersMapper ordersMapper;

    @Transactional
    public void reviewProduct(Long orderId, Long productId, Integer rating, String content, Long userId) {
        Orders o = ordersMapper.selectById(orderId);
        if (o == null || !o.getBuyerId().equals(userId)) throw new BusinessException("订单不存在");
        ProductReview r = new ProductReview();
        r.setOrderId(orderId);
        r.setProductId(productId);
        r.setUserId(userId);
        r.setRating(rating);
        r.setContent(content);
        productReviewMapper.insert(r);
        updateProductRating(productId);
    }

    @Transactional
    public void reviewMerchant(Long orderId, Long merchantId, Integer rating, String content, Long buyerId) {
        BuyerMerchantReview r = new BuyerMerchantReview();
        r.setOrderId(orderId);
        r.setBuyerId(buyerId);
        r.setMerchantId(merchantId);
        r.setRating(rating);
        r.setContent(content);
        buyerMerchantReviewMapper.insert(r);
        updateMerchantSatisfaction(merchantId);
    }

    @Transactional
    public void reviewBuyer(Long orderId, Long buyerId, Integer rating, String content, Long merchantId) {
        MerchantBuyerReview r = new MerchantBuyerReview();
        r.setOrderId(orderId);
        r.setMerchantId(merchantId);
        r.setBuyerId(buyerId);
        r.setRating(rating);
        r.setContent(content);
        merchantBuyerReviewMapper.insert(r);
    }

    private void updateProductRating(Long productId) {
        List<ProductReview> list = productReviewMapper.selectList(
                new LambdaQueryWrapper<ProductReview>().eq(ProductReview::getProductId, productId));
        double avg = list.stream().mapToInt(ProductReview::getRating).average().orElse(5.0);
        Product p = productMapper.selectById(productId);
        if (p != null) {
            p.setAvgRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
            productMapper.updateById(p);
        }
    }

    private void updateMerchantSatisfaction(Long merchantId) {
        List<BuyerMerchantReview> list = buyerMerchantReviewMapper.selectList(
                new LambdaQueryWrapper<BuyerMerchantReview>().eq(BuyerMerchantReview::getMerchantId, merchantId));
        double avg = list.stream().mapToInt(BuyerMerchantReview::getRating).average().orElse(5.0) * 20;
        MerchantInfo mi = merchantInfoMapper.selectOne(
                new LambdaQueryWrapper<MerchantInfo>().eq(MerchantInfo::getUserId, merchantId));
        if (mi != null) {
            mi.setSatisfactionRate(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
            merchantInfoMapper.updateById(mi);
        }
    }
}
