package com.campus.ershou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("merchant_ban")
public class MerchantBan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long merchantId;
    private String banType;
    private String reason;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
}
