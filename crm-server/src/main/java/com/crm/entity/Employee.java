package com.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Employee {
    private Long id;
    private String empNo;
    private String empName;
    @JsonIgnore
    private String password;
    private Long deptId;
    private Long roleId;
    private String phone;
    private String email;
    private Integer gender;
    private Integer status;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 连表带出的展示字段，不入库 */
    private String deptName;
    private String roleName;
}
