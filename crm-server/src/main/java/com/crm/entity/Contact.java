package com.crm.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Contact {
    private Long id;
    private Long customerId;
    private String contactName;
    private String position;
    private String phone;
    private String email;
    private String wechat;
    private Integer isPrimary;
    private String remark;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String customerName;
}
