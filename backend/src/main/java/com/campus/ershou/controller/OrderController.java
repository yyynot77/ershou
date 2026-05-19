package com.campus.ershou.controller;

import com.campus.ershou.common.Result;
import com.campus.ershou.common.UserContext;
import com.campus.ershou.dto.CheckoutDTO;
import com.campus.ershou.entity.Orders;
import com.campus.ershou.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单 Controller
 * <p>
 * 前端：
 * - 买家：Cart.vue 一键下单、ProductDetail 立即购买、Orders.vue 确认收货/退货
 * - 商家：MerchantOrders.vue 发货、审核退货
 * <p>
 * 下单调用链：
 * 用户点击结算 → checkout(CheckoutDTO)
 * → OrderService.checkout（扣库存、锁商品、扣钱包、写 orders/order_item）
 * → 删除已购商品对应购物车行 → 前端跳转 /orders
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired private OrderService orderService;

    /**
     * 创建订单并钱包支付
     * 前端：Cart.vue doCheckout / ProductDetail.doCheckout
     * body：cartItemIds 或 productId+quantity，可含 meetTime/meetPlace/usePoints
     */
    @PostMapping("/checkout")
    public Result<Orders> checkout(@RequestBody CheckoutDTO dto) {
        return Result.ok(orderService.checkout(UserContext.getUserId(), dto));
    }

    /** 买家订单列表 → Orders.vue */
    @GetMapping("/my")
    public Result<List<Map<String, Object>>> myOrders() {
        return Result.ok(orderService.myOrders(UserContext.getUserId()));
    }

    /** 商家订单列表 → MerchantOrders.vue */
    @GetMapping("/merchant")
    public Result<List<Map<String, Object>>> merchantOrders() {
        return Result.ok(orderService.merchantOrders(UserContext.getUserId()));
    }

    /**
     * 商家发货：PAID → SHIPPED
     * 前端：MerchantOrders.vue 发货按钮
     */
    @PostMapping("/{id}/ship")
    public Result<?> ship(@PathVariable Long id) {
        orderService.shipOrder(id, UserContext.getUserId());
        return Result.ok("已发货");
    }

    /**
     * 买家确认收货：触发结算、积分、商品 SOLD
     * 前端：Orders.vue 确认收货
     */
    @PostMapping("/{id}/receive")
    public Result<?> confirmReceive(@PathVariable Long id) {
        orderService.confirmReceive(id, UserContext.getUserId());
        return Result.ok();
    }

    /**
     * 买家申请退货（收货后 24 小时内）
     * 前端：Orders.vue 申请退货
     */
    @PostMapping("/{id}/return")
    public Result<?> requestReturn(@PathVariable Long id) {
        orderService.requestReturn(id, UserContext.getUserId());
        return Result.ok("退货申请已提交");
    }

    /**
     * 商家/管理员审核退货
     * 前端：MerchantOrders.vue pass=true/false
     */
    @PostMapping("/{id}/return/approve")
    public Result<?> approveReturn(@PathVariable Long id, @RequestParam boolean pass) {
        orderService.approveReturn(id, UserContext.getUserId(), pass);
        return Result.ok();
    }
}
