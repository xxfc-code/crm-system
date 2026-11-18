package com.crm.service;

import com.crm.common.BizException;
import com.crm.entity.Role;
import com.crm.mapper.RoleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleService {

    /** 系统里唯一能绕过普通授权的标识 */
    private static final String ADMIN_KEY = "admin";

    @Resource
    private RoleMapper roleMapper;

    public List<Role> list() {
        return roleMapper.selectList();
    }

    public void add(Role role) {
        if (ADMIN_KEY.equals(role.getRoleKey())) {
            throw new BizException("admin 是系统保留标识，换一个吧");
        }
        roleMapper.insert(role);
    }

    public void update(Role role) {
        Role old = roleMapper.selectById(role.getId());
        if (old == null) {
            throw new BizException("角色不存在");
        }
        // 只有原本就是 admin 的那个角色能保留这个标识，别的角色改过来等于自封超管
        if (ADMIN_KEY.equals(role.getRoleKey()) && !ADMIN_KEY.equals(old.getRoleKey())) {
            throw new BizException("admin 是系统保留标识，换一个吧");
        }
        roleMapper.update(role);
    }

    public void delete(Long id, boolean force) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BizException("角色不存在");
        }
        if ("admin".equals(role.getRoleKey())) {
            throw new BizException("超级管理员角色不能删除");
        }
        if (roleMapper.countEmployees(id) > 0) {
            throw new BizException("该角色下还有员工，不能删除");
        }
        if (force) {
            roleMapper.deleteForce(id);
        } else {
            roleMapper.deleteLogic(id);
        }
        roleMapper.deleteRoleMenu(id);
    }

    public List<Long> menuIds(Long roleId) {
        return roleMapper.selectMenuIds(roleId);
    }

    /**
     * 全删再全插，比算差集简单，授权是低频操作
     */
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        if (roleMapper.selectById(roleId) == null) {
            throw new BizException("角色不存在");
        }
        roleMapper.deleteRoleMenu(roleId);
        if (menuIds == null) {
            return;
        }
        for (Long menuId : menuIds) {
            roleMapper.insertRoleMenu(roleId, menuId);
        }
    }
}
