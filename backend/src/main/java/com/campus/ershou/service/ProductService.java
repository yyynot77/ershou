package com.campus.ershou.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.ershou.common.*;
import com.campus.ershou.dto.ProductDTO;
import com.campus.ershou.entity.*;
import com.campus.ershou.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {
    @Autowired private ProductMapper productMapper;
    @Autowired private ProductImageMapper imageMapper;
    @Autowired private MerchantInfoMapper merchantInfoMapper;
    @Autowired private SysUserMapper userMapper;
    @Autowired private MerchantBanMapper banMapper;
    @Autowired private ProductReviewMapper reviewMapper;

    @Transactional
    public Product publish(ProductDTO dto) {
        Long merchantId = UserContext.getUserId();
        checkMerchantCanPublish(merchantId);
        Product p = new Product();
        p.setMerchantId(merchantId);
        p.setCategoryId(dto.getCategoryId());
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setOriginalPrice(dto.getOriginalPrice());
        p.setPrice(dto.getPrice());
        p.setSizeInfo(dto.getSizeInfo());
        p.setConditionLevel(dto.getConditionLevel());
        p.setAllowBargain(dto.getAllowBargain() != null ? dto.getAllowBargain() : 0);
        p.setStock(dto.getStock() != null ? dto.getStock() : 1);
        p.setStatus(Constants.PRODUCT_PENDING);
        p.setAvgRating(new BigDecimal("5.00"));
        productMapper.insert(p);
        saveImages(p.getId(), dto.getImages());
        return p;
    }

    @Transactional
    public void update(ProductDTO dto) {
        Product p = productMapper.selectById(dto.getId());
        if (p == null || !p.getMerchantId().equals(UserContext.getUserId())) {
            throw new BusinessException("商品不存在或无权限");
        }
        p.setCategoryId(dto.getCategoryId());
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setOriginalPrice(dto.getOriginalPrice());
        p.setPrice(dto.getPrice());
        p.setSizeInfo(dto.getSizeInfo());
        p.setConditionLevel(dto.getConditionLevel());
        p.setAllowBargain(dto.getAllowBargain());
        p.setStock(dto.getStock());
        p.setStatus(Constants.PRODUCT_PENDING);
        productMapper.updateById(p);
        imageMapper.delete(new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getProductId, p.getId()));
        saveImages(p.getId(), dto.getImages());
    }

    public void offShelf(Long id) {
        Product p = productMapper.selectById(id);
        if (p == null || !p.getMerchantId().equals(UserContext.getUserId())) {
            throw new BusinessException("无权限");
        }
        p.setStatus(Constants.PRODUCT_OFF_SHELF);
        productMapper.updateById(p);
    }

    public void audit(Long id, boolean pass) {
        Product p = productMapper.selectById(id);
        if (p == null) throw new BusinessException("商品不存在");
        p.setStatus(pass ? Constants.PRODUCT_PUBLISHED : Constants.STATUS_REJECTED);
        productMapper.updateById(p);
    }

    public Map<String, Object> detail(Long id) {
        Product p = productMapper.selectById(id);
        if (p == null) throw new BusinessException("商品不存在");
        Map<String, Object> map = enrich(p);
        map.put("reviews", reviewMapper.selectList(
                new LambdaQueryWrapper<ProductReview>().eq(ProductReview::getProductId, id)
                        .orderByDesc(ProductReview::getCreateTime)));
        return map;
    }

    public Page<Map<String, Object>> search(String keyword, Long categoryId, String sortBy, int page, int size) {
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<>();
        qw.eq(Product::getStatus, Constants.PRODUCT_PUBLISHED);
        qw.gt(Product::getStock, 0);
        if (keyword != null && !keyword.isBlank()) {
            qw.like(Product::getName, keyword);
        }
        if (categoryId != null) {
            qw.eq(Product::getCategoryId, categoryId);
        }
        if ("price_asc".equals(sortBy)) qw.orderByAsc(Product::getPrice);
        else if ("price_desc".equals(sortBy)) qw.orderByDesc(Product::getPrice);
        else if ("sales".equals(sortBy)) qw.orderByDesc(Product::getSoldCount);
        else if ("rating".equals(sortBy)) qw.orderByDesc(Product::getAvgRating);
        else qw.orderByDesc(Product::getCreateTime);
        Page<Product> pp = productMapper.selectPage(new Page<>(page, size), qw);
        Page<Map<String, Object>> result = new Page<>(page, size, pp.getTotal());
        result.setRecords(pp.getRecords().stream().map(this::enrich).collect(Collectors.toList()));
        return result;
    }

    public List<Map<String, Object>> merchantProducts(String status) {
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, UserContext.getUserId());
        if (status != null && !status.isBlank()) qw.eq(Product::getStatus, status);
        qw.orderByDesc(Product::getCreateTime);
        return productMapper.selectList(qw).stream().map(this::enrich).collect(Collectors.toList());
    }

    public List<Map<String, Object>> shopProducts(Long merchantId) {
        List<Product> list = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, merchantId)
                .in(Product::getStatus, Constants.PRODUCT_PUBLISHED, Constants.PRODUCT_SOLD)
                .orderByDesc(Product::getCreateTime));
        return list.stream().map(this::enrich).collect(Collectors.toList());
    }

    public Map<String, Object> shopInfo(Long merchantId) {
        MerchantInfo mi = merchantInfoMapper.selectOne(
                new LambdaQueryWrapper<MerchantInfo>().eq(MerchantInfo::getUserId, merchantId));
        SysUser u = userMapper.selectById(merchantId);
        Map<String, Object> map = new HashMap<>();
        map.put("merchant", mi);
        map.put("user", u);
        map.put("products", shopProducts(merchantId));
        return map;
    }

    private Map<String, Object> enrich(Product p) {
        Map<String, Object> m = new HashMap<>();
        m.put("product", p);
        m.put("images", imageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getProductId, p.getId())
                        .orderByAsc(ProductImage::getSortOrder)));
        MerchantInfo mi = merchantInfoMapper.selectOne(
                new LambdaQueryWrapper<MerchantInfo>().eq(MerchantInfo::getUserId, p.getMerchantId()));
        m.put("shopName", mi != null ? mi.getShopName() : "未知店铺");
        m.put("merchantLevel", mi != null ? mi.getMerchantLevel() : 1);
        return m;
    }

    private void saveImages(Long productId, List<String> images) {
        if (images == null) return;
        int i = 0;
        for (String url : images) {
            ProductImage img = new ProductImage();
            img.setProductId(productId);
            img.setImageUrl(url);
            img.setSortOrder(i++);
            imageMapper.insert(img);
        }
    }

    private void checkMerchantCanPublish(Long merchantId) {
        List<MerchantBan> bans = banMapper.selectList(new LambdaQueryWrapper<MerchantBan>()
                .eq(MerchantBan::getMerchantId, merchantId)
                .and(w -> w.isNull(MerchantBan::getEndTime).or().gt(MerchantBan::getEndTime, LocalDateTime.now())));
        for (MerchantBan b : bans) {
            if ("PUBLISH_BAN".equals(b.getBanType()) || "SHOP_CLOSE".equals(b.getBanType())) {
                throw new BusinessException("店铺已被限制，无法发布商品");
            }
        }
    }
}
