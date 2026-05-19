package com.campus.ershou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("product_review")
public class ProductReview {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Long orderId;
    private Long userId;
    private Integer rating;
    private String content;
    private LocalDateTime createTime;
}
