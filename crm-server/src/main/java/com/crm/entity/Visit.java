package com.crm.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Visit {
    private Long id;
    private Long customerId;
    private LocalDate visitDate;
    private Long visitorId;
    private String way;
    private String content;
    private Integer satisfaction;
    private LocalDate nextTime;
    private String remark;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String customerName;
    private String visitorName;
}
