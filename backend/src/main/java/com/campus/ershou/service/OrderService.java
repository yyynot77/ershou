package com.campus.ershou.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.ershou.common.*;
import com.campus.ershou.dto.CheckoutDTO;
import com.campus.ershou.entity.*;
import com.campus.ershou.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {
    @Autowired private OrdersMapper ordersMapper;
    @Autowired private OrderItemMapper orderItemMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private CartItemMapper cartMapper;
    @Autowired private MerchantInfoMapper merchantInfoMapper;
    @Autowired private PlatformAccountMapper platformMapper;
    @Autowired private BuyerBlacklistMapper blacklistMapper;
    @Autowired private WalletService walletService;
    @Autowired private PointsService pointsService;

    @Value("${app.order.return-hours:24}")
    private int returnHours;

    @Transactional
    public Orders checkout(Long buyerId, CheckoutDTO dto) {
        checkBlacklist(buyerId, null);
        List<CartItem> cartItems = new ArrayList<>();
        if (dto.getCartItemIds() != null && !dto.getCartItemIds().isEmpty()) {
            cartItems = cartMapper.selectBatchIds(dto.getCartItemIds());
            cartItems.removeIf(c -> !c.getUserId().equals(buyerId));
        } else if (dto.getProductId() != null) {
            CartItem c = new CartItem();
            c.setProductId(dto.getProductId());
            c.setQuantity(dto.getQuantity() != null ? dto.getQuantity() : 1);
            cartItems.add(c);
        }
        if (cartItems.isEmpty()) throw new BusinessException("无结算商品");

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();
        for (CartItem ci : cartItems) {
            Product p = productMapper.selectById(ci.getProductId());
            if (p == null || !Constants.PRODUCT_PUBLISHED.equals(p.getStatus())) {
                throw new BusinessException("商品不可购买: " + (p != null ? p.getName() : ci.getProductId()));
            }
            if (p.getStock() < ci.getQuantity()) throw new BusinessException("库存不足: " + p.getName());
            checkBlacklist(buyerId, p.getMerchantId());
            BigDecimal sub = p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            total = total.add(sub);
            OrderItem oi = new OrderItem();
            oi.setProductId(p.getId());
            oi.setMerchantId(p.getMerchantId());
            oi.setProductName(p.getName());
            oi.setPrice(p.getPrice());
            oi.setQuantity(ci.getQuantity());
            oi.setSubtotal(sub);
            oi.setSettleStatus("ESCROW");
            items.add(oi);
            p.setStock(p.getStock() - ci.getQuantity());
            if (p.getStock() <= 0) {
                p.setStatus(Constants.PRODUCT_SOLD);
            } else {
                p.setStatus(Constants.PRODUCT_LOCKED);
            }
            productMapper.updateById(p);
        }

        int usePoints = dto.getUsePoints() != null ? dto.getUsePoints() : 0;
        BigDecimal deduct = pointsService.calcDeductAmount(usePoints);
        if (deduct.compareTo(total) > 0) deduct = total;
        BigDecimal pay = total.subtract(deduct);
        if (usePoints > 0) pointsService.redeem(buyerId, usePoints);

        Orders order = new Orders();
        order.setOrderNo("O" + System.currentTimeMillis() + buyerId);
        order.setBuyerId(buyerId);
        order.setTotalAmount(total);
        order.setPointsDeduct(deduct);
        order.setPayAmount(pay);
        order.setPlatformFee(BigDecimal.ZERO);
        order.setStatus("PAID");
        order.setMeetTime(dto.getMeetTime());
        order.setMeetPlace(dto.getMeetPlace());
        ordersMapper.insert(order);

        BigDecimal totalFee = BigDecimal.ZERO;
        for (OrderItem oi : items) {
            oi.setOrderId(order.getId());
            orderItemMapper.insert(oi);
            double rate = Constants.MERCHANT_FEE_RATE[getMerchantLevel(oi.getMerchantId())];
            totalFee = totalFee.add(oi.getSubtotal().multiply(BigDecimal.valueOf(rate)));
        }
        order.setPlatformFee(totalFee);
        ordersMapper.updateById(order);
        walletService.pay(buyerId, pay, order.getId());
        addEscrow(pay);
        if (dto.getCartItemIds() != null) {
            cartMapper.deleteBatchIds(dto.getCartItemIds());
        }
        return order;
    }

    public void shipOrder(Long orderId, Long merchantId) {
        Orders o = ordersMapper.selectById(orderId);
        if (o == null) throw new BusinessException("订单不存在");
        if (!"PAID".equals(o.getStatus())) {
            throw new BusinessException("仅已付款订单可发货");
        }
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        boolean hasMerchant = items.stream().anyMatch(i -> i.getMerchantId().equals(merchantId));
        if (!hasMerchant) throw new BusinessException("无权限操作该订单");
        o.setStatus("SHIPPED");
        ordersMapper.updateById(o);
    }

    public void confirmReceive(Long orderId, Long buyerId) {
        Orders o = ordersMapper.selectById(orderId);
        if (o == null || !o.getBuyerId().equals(buyerId)) throw new BusinessException("订单不存在");
        if (!"PAID".equals(o.getStatus()) && !"SHIPPED".equals(o.getStatus())) {
            throw new BusinessException("当前状态不可确认收货");
        }
        o.setStatus("RECEIVED");
        o.setReceiveTime(LocalDateTime.now());
        ordersMapper.updateById(o);
        settleOrder(o);
        pointsService.earn(buyerId, o.getPayAmount(), orderId);
    }

    @Transactional
    public void requestReturn(Long orderId, Long buyerId) {
        Orders o = ordersMapper.selectById(orderId);
        if (o == null || !o.getBuyerId().equals(buyerId)) throw new BusinessException("订单不存在");
        if (!"RECEIVED".equals(o.getStatus()) && !"COMPLETED".equals(o.getStatus())) {
            throw new BusinessException("仅已收货订单可申请退货");
        }
        if (o.getReceiveTime() == null || o.getReceiveTime().plusHours(returnHours).isBefore(LocalDateTime.now())) {
            throw new BusinessException("已超过" + returnHours + "小时，无法申请退货");
        }
        o.setStatus("RETURN_REQUEST");
        ordersMapper.updateById(o);
    }

    @Transactional
    public void approveReturn(Long orderId, Long merchantId, boolean pass) {
        Orders o = ordersMapper.selectById(orderId);
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        boolean hasMerchant = items.stream().anyMatch(i -> i.getMerchantId().equals(merchantId));
        if (!hasMerchant && !Constants.ROLE_ADMIN.equals(UserContext.getRole())) {
            throw new BusinessException("无权限");
        }
        if (!"RETURN_REQUEST".equals(o.getStatus())) throw new BusinessException("非退货申请状态");
        if (pass) {
            o.setStatus("RETURNED");
            ordersMapper.updateById(o);
            walletService.refund(o.getBuyerId(), o.getPayAmount(), orderId);
            for (OrderItem oi : items) {
                Product p = productMapper.selectById(oi.getProductId());
                if (p != null) {
                    p.setStock(p.getStock() + oi.getQuantity());
                    p.setStatus(Constants.PRODUCT_PUBLISHED);
                    productMapper.updateById(p);
                }
                oi.setSettleStatus("REFUNDED");
                orderItemMapper.updateById(oi);
            }
        } else {
            o.setStatus("COMPLETED");
            ordersMapper.updateById(o);
        }
    }

    public List<Map<String, Object>> myOrders(Long userId) {
        List<Orders> list = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getBuyerId, userId).orderByDesc(Orders::getCreateTime));
        return wrapOrders(list);
    }

    public List<Map<String, Object>> merchantOrders(Long merchantId) {
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getMerchantId, merchantId));
        Set<Long> orderIds = new HashSet<>();
        items.forEach(i -> orderIds.add(i.getOrderId()));
        if (orderIds.isEmpty()) return List.of();
        List<Orders> list = ordersMapper.selectBatchIds(orderIds);
        list.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
        return wrapOrders(list);
    }

    @Transactional
    public void autoConfirmOrders(int days) {
        LocalDateTime deadline = LocalDateTime.now().minusDays(days);
        List<Orders> list = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .in(Orders::getStatus, "PAID", "SHIPPED")
                .lt(Orders::getCreateTime, deadline));
        for (Orders o : list) {
            o.setStatus("RECEIVED");
            o.setReceiveTime(LocalDateTime.now());
            ordersMapper.updateById(o);
            settleOrder(o);
            pointsService.earn(o.getBuyerId(), o.getPayAmount(), o.getId());
        }
    }

    private void settleOrder(Orders o) {
        if ("COMPLETED".equals(o.getStatus())) return;
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, o.getId()));
        for (OrderItem oi : items) {
            if ("SETTLED".equals(oi.getSettleStatus()) || "REFUNDED".equals(oi.getSettleStatus())) continue;
            int level = getMerchantLevel(oi.getMerchantId());
            double rate = Constants.MERCHANT_FEE_RATE[level];
            BigDecimal fee = oi.getSubtotal().multiply(BigDecimal.valueOf(rate)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal sellerAmount = oi.getSubtotal().subtract(fee);
            walletService.settleToSeller(oi.getMerchantId(), sellerAmount, o.getId());
            oi.setSettleStatus("SETTLED");
            oi.setSettleTime(LocalDateTime.now());
            orderItemMapper.updateById(oi);
            Product p = productMapper.selectById(oi.getProductId());
            if (p != null) {
                p.setSoldCount(p.getSoldCount() + oi.getQuantity());
                p.setStatus(Constants.PRODUCT_SOLD);
                productMapper.updateById(p);
            }
            MerchantInfo mi = merchantInfoMapper.selectOne(
                    new LambdaQueryWrapper<MerchantInfo>().eq(MerchantInfo::getUserId, oi.getMerchantId()));
            if (mi != null) {
                mi.setTotalSales(mi.getTotalSales().add(oi.getSubtotal()));
                merchantInfoMapper.updateById(mi);
            }
            addFeeIncome(fee);
        }
        o.setStatus("COMPLETED");
        ordersMapper.updateById(o);
    }

    private int getMerchantLevel(Long merchantId) {
        MerchantInfo mi = merchantInfoMapper.selectOne(
                new LambdaQueryWrapper<MerchantInfo>().eq(MerchantInfo::getUserId, merchantId));
        if (mi == null || mi.getMerchantLevel() == null) return 1;
        int lv = mi.getMerchantLevel();
        return Math.min(Math.max(lv, 1), 5);
    }

    private void checkBlacklist(Long buyerId, Long merchantId) {
        long count = blacklistMapper.selectCount(new LambdaQueryWrapper<BuyerBlacklist>()
                .eq(BuyerBlacklist::getBuyerId, buyerId)
                .and(w -> w.isNull(BuyerBlacklist::getMerchantId)
                        .or().eq(merchantId != null, BuyerBlacklist::getMerchantId, merchantId)));
        if (count > 0) throw new BusinessException("您已被限制购买");
    }

    private void addEscrow(BigDecimal amount) {
        PlatformAccount pa = platformMapper.selectById(1);
        if (pa == null) return;
        pa.setEscrowBalance(pa.getEscrowBalance().add(amount));
        platformMapper.updateById(pa);
    }

    private void addFeeIncome(BigDecimal fee) {
        PlatformAccount pa = platformMapper.selectById(1);
        if (pa == null) return;
        pa.setFeeIncome(pa.getFeeIncome().add(fee));
        pa.setEscrowBalance(pa.getEscrowBalance().subtract(fee));
        platformMapper.updateById(pa);
    }

    private List<Map<String, Object>> wrapOrders(List<Orders> list) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Orders o : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("order", o);
            m.put("items", orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, o.getId())));
            result.add(m);
        }
        return result;
    }
}
