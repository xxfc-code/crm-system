package com.crm.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Delivery {
    private Long id;
    private String deliveryNo;
    private Long orderId;
    private Long customerId;
    private LocalDate deliveryDate;
    private String receiver;
    private String phone;
    private String address;
    private String status;
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
