package com.crm.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Opportunity {
    private Long id;
    private String oppNo;
    private String oppName;
    private Long customerId;
    private BigDecimal amount;
    private String stage;
    private Integer winRate;
    private LocalDate expectDate;
    private Long ownerId;
    private Long deptId;
    private String remark;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String customerName;
    private String ownerName;
    private List<OpportunityStageLog> stageLogs = new ArrayList<>();
}
