package com.campus.ershou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.ershou.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车表 cart_item（MyBatis-Plus BaseMapper，无 XML）
 * 业务调用：CartService
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {}
