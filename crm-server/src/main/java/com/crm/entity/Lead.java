package com.crm.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Lead {
    private Long id;
    private String leadNo;
    private String leadName;
    private String company;
    private String phone;
    private String source;
    private Integer status;
    private Long ownerId;
    private Long deptId;
    private Long customerId;
    private LocalDateTime nextTime;
    private LocalDateTime lastFollowTime;
    private LocalDateTime poolTime;
    private Long preOwnerId;
    private String remark;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String ownerName;
    private String deptName;
    private String preOwnerName;
}
