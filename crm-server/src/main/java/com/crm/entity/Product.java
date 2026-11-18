package com.crm.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {
    private Long id;
    private String productNo;
    private String productName;
    private Long categoryId;
    private String spec;
    private String unit;
    private BigDecimal price;
    private BigDecimal cost;
    private Integer stock;
    private Integer status;
    private String remark;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String categoryName;
}
