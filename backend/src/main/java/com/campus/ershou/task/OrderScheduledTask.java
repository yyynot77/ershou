package com.campus.ershou.task;

import com.campus.ershou.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
