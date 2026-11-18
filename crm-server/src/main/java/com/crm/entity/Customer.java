package com.crm.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Customer {
    private Long id;
    private String customerNo;
    private String storeNo;
    private LocalDate signDate;
    private String region;
    private LocalDate firstOrderDate;
    private BigDecimal storeArea;
    private String customerName;
    private String contact;
    private String phone;
    private String email;
    private String level;
    private String industry;
    private String address;
    private String source;
    private Long ownerId;
    private Long deptId;
    private Long leadId;
    private String remark;
    private LocalDateTime lastFollowTime;
    private LocalDateTime poolTime;
    private Long preOwnerId;
    private String poolReason;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String ownerName;
    private String deptName;
    private String preOwnerName;
    private Long oppCount;
    private Long orderCount;
}
