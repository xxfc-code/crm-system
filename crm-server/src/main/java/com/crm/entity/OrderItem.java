package com.crm.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private String spec;
    private String unit;
    private Integer qty;
    private BigDecimal price;
    /** 下单时的成本快照，后面调价不会影响历史订单毛利 */
    private BigDecimal cost;
    private BigDecimal amount;
}
