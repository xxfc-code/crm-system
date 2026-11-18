package com.crm.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Payment {
    private Long id;
    private String paymentNo;
    private Long orderId;
    /** 来源退换货单，用来防止重复冲销 */
    private Long returnId;
    private Long customerId;
    private BigDecimal amount;
    private LocalDate payDate;
    private String payType;
    private Long ownerId;
    private Long deptId;
    private String remark;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String customerName;
    private String ownerName;
    private String orderNo;
}
