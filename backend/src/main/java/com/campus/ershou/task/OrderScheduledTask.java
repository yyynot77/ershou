package com.campus.ershou.task;

import com.campus.ershou.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单定时任务
 * <p>
 * 每天凌晨 2 点：将超期未确认收货的 PAID/SHIPPED 订单自动确认 → OrderService.autoConfirmOrders
 * <p>
 * 配置项：app.order.auto-confirm-days（默认 7 天）
 * TODO：与买家手动 confirmReceive 共用 settleOrder，需保证幂等
 */
@Component
public class OrderScheduledTask {
    @Autowired
    private OrderService orderService;
    @Value("${app.order.auto-confirm-days:7}")
    private int autoConfirmDays;

    @Scheduled(cron = "0 0 2 * * ?")
    public void autoConfirm() {
        orderService.autoConfirmOrders(autoConfirmDays);
    }
}
