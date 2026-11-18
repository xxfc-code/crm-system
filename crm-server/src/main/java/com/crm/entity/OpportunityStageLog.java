package com.crm.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商机阶段流转记录，用来算阶段转化率和平均停留时长
 */
@Data
public class OpportunityStageLog {
    private Long id;
    private Long opportunityId;
    private String fromStage;
    private String toStage;
    private Integer winRate;
    private Long operatorId;
    private String remark;
    private LocalDateTime createTime;

    private String operatorName;
}
