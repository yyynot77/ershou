package com.campus.ershou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Orders {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long buyerId;
    private BigDecimal totalAmount;
    private BigDecimal pointsDeduct;
    private BigDecimal payAmount;
    private BigDecimal platformFee;
    private String status;
    private String meetTime;
    private String meetPlace;
    private LocalDateTime receiveTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
