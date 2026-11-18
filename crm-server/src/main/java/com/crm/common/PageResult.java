package com.crm.common;

import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 分页返回，前端只用 list 和 total
 */
public record PageResult<T>(List<T> list, long total) {

    public static <T> PageResult<T> of(List<T> rows) {
        PageInfo<T> info = new PageInfo<>(rows);
        return new PageResult<>(info.getList(), info.getTotal());
    }
}
