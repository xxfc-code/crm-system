package com.crm.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 当前登录人的数据可见范围，查询时拼进 SQL
 * 1 全部 / 2 本部门 / 3 本部门及以下 / 4 仅本人
 */
@Data
@AllArgsConstructor
public class DataScope {

    private int scope;
    private Long userId;
    private List<Long> deptIds;

    public boolean isAll() {
        return scope == 1;
    }

    /**
     * 判断某条数据能不能被当前人访问
     * ownerId 为空表示公海/无主数据，谁都能看
     */
    public boolean canAccess(Long ownerId, Long deptId) {
        if (scope == 1) {
            return true;
        }
        if (ownerId == null) {
            return true;
        }
        if (scope == 4) {
            return ownerId.equals(userId);
        }
        return deptId != null && deptIds.contains(deptId);
    }
}
