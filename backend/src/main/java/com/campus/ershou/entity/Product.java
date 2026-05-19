package com.campus.ershou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long merchantId;
    private Long categoryId;
    private String name;
    private String description;
    private BigDecimal originalPrice;
    private BigDecimal price;
    private String sizeInfo;
    private String conditionLevel;
    private Integer allowBargain;
    private Integer stock;
    private Integer soldCount;
    private String status;
    private BigDecimal avgRating;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
