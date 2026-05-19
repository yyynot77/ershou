package com.campus.ershou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("platform_account")
public class PlatformAccount {
    @TableId
    private Integer id;
    private BigDecimal escrowBalance;
    private BigDecimal feeIncome;
}
