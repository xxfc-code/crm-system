package com.crm.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CrmOrder {
    private Long id;
    private String orderNo;
    private Long customerId;
    private Long oppId;
    private Long quoteId;
    private BigDecimal amount;
    private String status;
    private LocalDate signDate;
    private Long ownerId;
    private Long deptId;
    private String remark;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String customerName;
    private String ownerName;
    private String oppName;

    /** 最近一次回款日期，由收款流水汇总而来 */
    private LocalDate lastPayDate;
    /** 已收金额合计 */
    private BigDecimal paidAmount;
    /** 明细成本合计 */
    private BigDecimal costTotal;
    /** 毛利 = 订单金额 - 成本合计 */
    private BigDecimal grossProfit;

    private List<OrderItem> items = new ArrayList<>();
}
