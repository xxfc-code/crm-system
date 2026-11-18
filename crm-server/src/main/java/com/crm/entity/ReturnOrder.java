package com.crm.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReturnOrder {
    private Long id;
    private String returnNo;
    private Long orderId;
    private Long customerId;
    private String returnType;
    private String reason;
    private BigDecimal amount;
    private String status;
    private LocalDate handleDate;
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
