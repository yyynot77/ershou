package com.campus.ershou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("buyer_blacklist")
public class BuyerBlacklist {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long buyerId;
    private Long merchantId;
    private String reason;
    private LocalDateTime createTime;
}
