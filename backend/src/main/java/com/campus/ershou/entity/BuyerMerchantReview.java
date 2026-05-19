package com.campus.ershou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("buyer_merchant_review")
public class BuyerMerchantReview {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long buyerId;
    private Long merchantId;
    private Integer rating;
    private String content;
    private LocalDateTime createTime;
}
