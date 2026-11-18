package com.crm.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductCategory {
    private Long id;
    private String categoryName;
    private Long parentId;
    private Integer sort;
    private Integer status;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private Long productCount;
}
