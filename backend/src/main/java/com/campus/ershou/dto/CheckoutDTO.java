package com.campus.ershou.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CheckoutDTO {
    private List<Long> cartItemIds;
    private Long productId;
    private Integer quantity;
    private Integer usePoints;
    private String meetTime;
    private String meetPlace;
    private BigDecimal pointsDeduct;
}
