package com.crm.service;

import com.crm.common.BizException;
import com.crm.common.DataScope;
import com.crm.common.UserContext;
import com.crm.entity.Dept;
import com.crm.entity.Employee;
import com.crm.entity.Role;
import com.crm.mapper.DeptMapper;
import com.crm.mapper.EmployeeMapper;
import com.crm.mapper.RoleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DataScopeService {

    @Resource
    private EmployeeMapper employeeMapper;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private DeptMapper deptMapper;

    /**
     * 一次请求里可能查很多次（列表 + 详情 + 改），解析结果放 ThreadLocal 复用，请求结束由拦截器清理
     */
    private static final ThreadLocal<DataScope> CACHE = new ThreadLocal<>();

    public DataScope current() {
        DataScope cached = CACHE.get();
        if (cached != null) {
            return cached;
        }
        DataScope resolved = resolve();
        CACHE.set(resolved);
        return resolved;
    }

    /**
     * 按 id 做单条数据校验，越权直接抛给全局异常处理
     */
    public void check(Long ownerId, Long deptId) {
        if (!current().canAccess(ownerId, deptId)) {
            throw new BizException("没有权限查看或操作这条数据");
        }
    }

    /** 当前登录人，新增数据没填负责人时用来兜底 */
    public Employee me() {
        return employeeMapper.selectById(UserContext.get());
    }

    public void clear() {
        CACHE.remove();
    }

    private DataScope resolve() {
        Long userId = UserContext.get();
        Employee emp = employeeMapper.selectById(userId);
        if (emp == null) {
            return new DataScope(4, userId, List.of());
        }

        Role role = roleMapper.selectById(emp.getRoleId());
        int scope = role == null || role.getDataScope() == null ? 4 : role.getDataScope();

        if (scope == 1) {
            return new DataScope(1, userId, List.of());
        }
        // 仅本人不依赖部门，要在部门判断之前返回
        if (scope == 4) {
            return new DataScope(4, userId, List.of());
        }
        if (emp.getDeptId() == null) {
            return new DataScope(scope, userId, List.of());
        }
        if (scope == 2) {
            return new DataScope(2, userId, List.of(emp.getDeptId()));
        }
        return new DataScope(3, userId, deptAndChildren(emp.getDeptId()));
    }

    /**
     * 本部门 + 所有下级。部门就几十行，内存里滚几遍比递归查库快
     */
    private List<Long> deptAndChildren(Long rootId) {
        List<Dept> all = deptMapper.selectAll();
        List<Long> ids = new ArrayList<>();
        ids.add(rootId);

        boolean grew = true;
        while (grew) {
            grew = false;
            for (Dept dept : all) {
                if (ids.contains(dept.getParentId()) && !ids.contains(dept.getId())) {
                    ids.add(dept.getId());
                    grew = true;
                }
            }
        }
        return ids;
    }
}
