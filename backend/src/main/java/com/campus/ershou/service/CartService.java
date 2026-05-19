package com.campus.ershou.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.ershou.common.BusinessException;
import com.campus.ershou.common.Constants;
import com.campus.ershou.common.UserContext;
import com.campus.ershou.entity.*;
import com.campus.ershou.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartService {
    @Autowired private CartItemMapper cartMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private ProductImageMapper imageMapper;
    @Autowired private MerchantInfoMapper merchantInfoMapper;

    public void add(Long productId, Integer quantity) {
        Long userId = UserContext.getUserId();
        Product p = productMapper.selectById(productId);
        if (p == null || !Constants.PRODUCT_PUBLISHED.equals(p.getStatus())) {
            throw new BusinessException("商品不可加入购物车");
        }
        CartItem exist = cartMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId).eq(CartItem::getProductId, productId));
        if (exist != null) {
            exist.setQuantity(exist.getQuantity() + (quantity != null ? quantity : 1));
            cartMapper.updateById(exist);
        } else {
            CartItem c = new CartItem();
            c.setUserId(userId);
            c.setProductId(productId);
            c.setQuantity(quantity != null ? quantity : 1);
            cartMapper.insert(c);
        }
    }

    public void updateQty(Long id, Integer quantity) {
        CartItem c = cartMapper.selectById(id);
        if (c == null || !c.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException("购物车项不存在");
        }
        Product p = productMapper.selectById(c.getProductId());
        if (p == null || !Constants.PRODUCT_PUBLISHED.equals(p.getStatus()) || p.getStock() <= 0) {
            cartMapper.deleteById(id);
            throw new BusinessException("商品已下架或售罄，已从购物车移除");
        }
        if (quantity == null || quantity < 1) quantity = 1;
        if (quantity > p.getStock()) quantity = p.getStock();
        c.setQuantity(quantity);
        cartMapper.updateById(c);
    }

    public void remove(Long id) {
        cartMapper.delete(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getId, id).eq(CartItem::getUserId, UserContext.getUserId()));
    }

    public List<Map<String, Object>> list() {
        Long userId = UserContext.getUserId();
        List<CartItem> items = cartMapper.selectList(
                new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId));
        List<Map<String, Object>> result = new ArrayList<>();
        for (CartItem ci : items) {
            Product p = productMapper.selectById(ci.getProductId());
            if (p == null || !Constants.PRODUCT_PUBLISHED.equals(p.getStatus()) || p.getStock() <= 0) {
                cartMapper.deleteById(ci.getId());
                continue;
            }
            if (ci.getQuantity() > p.getStock()) {
                ci.setQuantity(p.getStock());
                cartMapper.updateById(ci);
            }
            Map<String, Object> m = new HashMap<>();
            m.put("cartItem", ci);
            m.put("product", p);
            ProductImage img = imageMapper.selectOne(new LambdaQueryWrapper<ProductImage>()
                    .eq(ProductImage::getProductId, ci.getProductId()).last("LIMIT 1"));
            m.put("image", img != null ? img.getImageUrl() : null);
            MerchantInfo mi = merchantInfoMapper.selectOne(
                    new LambdaQueryWrapper<MerchantInfo>().eq(MerchantInfo::getUserId, p.getMerchantId()));
            m.put("shopName", mi != null ? mi.getShopName() : "");
            result.add(m);
        }
        return result;
    }
}
