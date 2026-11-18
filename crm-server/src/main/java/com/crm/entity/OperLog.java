package com.crm.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperLog {
    private Long id;
    private Long operUserId;
    private String operUserName;
    private String module;
    private String operation;
    private String method;
    private String params;
    private String ip;
    private Long costMs;
    private Integer success;
    private String errorMsg;
    private LocalDateTime createTime;
}
