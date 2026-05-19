package com.campus.ershou.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDTO {
    private Long id;
    private Long categoryId;
    @NotBlank private String name;
    private String description;
    private BigDecimal originalPrice;
    @NotNull private BigDecimal price;
    private String sizeInfo;
    private String conditionLevel;
    private Integer allowBargain;
    @Min(1) private Integer stock;
    private List<String> images;
}
