package com.crm.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Ticket {
    private Long id;
    private String ticketNo;
    private Long customerId;
    private Long orderId;
    private Long productId;
    private String title;
    private String ticketType;
    private String priority;
    private String status;
    private String content;
    private String solution;
    private Long ownerId;
    private Long deptId;
    private LocalDateTime closeTime;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String customerName;
    private String ownerName;
}
