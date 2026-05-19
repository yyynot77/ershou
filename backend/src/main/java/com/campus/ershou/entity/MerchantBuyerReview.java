package com.campus.ershou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("merchant_buyer_review")
public class MerchantBuyerReview {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long merchantId;
    private Long buyerId;
    private Integer rating;
    private String content;
    private LocalDateTime createTime;
}
