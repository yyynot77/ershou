package com.campus.ershou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("merchant_info")
public class MerchantInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String shopName;
    private String businessLicense;
    private String idCardImage;
    private Integer merchantLevel;
    private BigDecimal satisfactionRate;
    private BigDecimal totalSales;
    private LocalDateTime createTime;
}
