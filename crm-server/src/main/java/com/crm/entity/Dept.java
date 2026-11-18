package com.crm.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 部门实体，children 不入库，只在组装树时填充
 */
@Data
public class Dept {
    private Long id;
    private Long parentId;
    private String deptName;
    private String leader;
    private String phone;
    private Integer sort;
    private Integer status;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private List<Dept> children = new ArrayList<>();
}
