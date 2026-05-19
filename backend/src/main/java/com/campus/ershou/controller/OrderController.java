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

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired private OrderService orderService;

    @PostMapping("/checkout")
    public Result<Orders> checkout(@RequestBody CheckoutDTO dto) {
        return Result.ok(orderService.checkout(UserContext.getUserId(), dto));
    }

    @GetMapping("/my")
    public Result<List<Map<String, Object>>> myOrders() {
        return Result.ok(orderService.myOrders(UserContext.getUserId()));
    }

    @GetMapping("/merchant")
    public Result<List<Map<String, Object>>> merchantOrders() {
        return Result.ok(orderService.merchantOrders(UserContext.getUserId()));
    }

    @PostMapping("/{id}/ship")
    public Result<?> ship(@PathVariable Long id) {
        orderService.shipOrder(id, UserContext.getUserId());
        return Result.ok("已发货");
    }

    @PostMapping("/{id}/receive")
    public Result<?> confirmReceive(@PathVariable Long id) {
        orderService.confirmReceive(id, UserContext.getUserId());
        return Result.ok();
    }

    @PostMapping("/{id}/return")
    public Result<?> requestReturn(@PathVariable Long id) {
        orderService.requestReturn(id, UserContext.getUserId());
        return Result.ok("退货申请已提交");
    }

    @PostMapping("/{id}/return/approve")
    public Result<?> approveReturn(@PathVariable Long id, @RequestParam boolean pass) {
        orderService.approveReturn(id, UserContext.getUserId(), pass);
        return Result.ok();
    }
}
