package com.crm.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Follow {
    private Long id;
    private String bizType;
    private Long bizId;
    private String content;
    private LocalDateTime nextTime;
    private Long createBy;
    private LocalDateTime createTime;

    private String createByName;
    private String bizName;
}
