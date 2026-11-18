package com.crm.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Quotation {
    private Long id;
    private String quoteNo;
    private Long customerId;
    private Long oppId;
    private LocalDate quoteDate;
    private LocalDate validUntil;
    private BigDecimal totalAmount;
    private String status;
    /** 已过有效期 */
    private Integer expired;
    /** 0免审 1待审批 2已通过 3已驳回 */
    private Integer approveStatus;
    private Long approveUserId;
    private LocalDateTime approveTime;
    private String approveRemark;
    private Long ownerId;
    private Long deptId;
    private String remark;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String customerName;
    private String ownerName;
    /** 已转成订单的次数，>0 表示不能再转 */
    private Integer convertedCount;
    private List<QuotationItem> items = new ArrayList<>();
}
