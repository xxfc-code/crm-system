package com.crm.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CustomerPrice {
    private Long id;
    private Long customerId;
    private Long productId;
    private BigDecimal price;
    private String remark;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String customerName;
    private String productName;
    private BigDecimal standardPrice;
}
