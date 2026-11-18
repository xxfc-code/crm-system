package com.crm.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuotationItem {
    private Long id;
    private Long quotationId;
    private Long productId;
    private String productName;
    private Integer qty;
    private BigDecimal price;
    /** 成本快照，算报价毛利用 */
    private BigDecimal cost;
    private BigDecimal amount;
}
