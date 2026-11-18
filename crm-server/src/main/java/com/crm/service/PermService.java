package com.crm.service;

import com.crm.common.UserContext;
import com.crm.entity.Employee;
import com.crm.entity.Menu;
import com.crm.entity.Role;
import com.crm.mapper.EmployeeMapper;
import com.crm.mapper.MenuMapper;
import com.crm.mapper.RoleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermService {

    @Resource
    private EmployeeMapper employeeMapper;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private MenuMapper menuMapper;

    /** 同一次请求可能校验多个接口，权限集合放 ThreadLocal 复用 */
    private static final ThreadLocal<Set<String>> CACHE = new ThreadLocal<>();

    public boolean has(String perm) {
        return perms().contains(perm);
    }

    public Set<String> perms() {
        Set<String> cached = CACHE.get();
        if (cached != null) {
            return cached;
        }
        Set<String> loaded = load();
        CACHE.set(loaded);
        return loaded;
    }

    public void clear() {
        CACHE.remove();
    }

    private Set<String> load() {
        Employee emp = employeeMapper.selectById(UserContext.get());
        if (emp == null || emp.getRoleId() == null) {
            return Set.of();
        }
        Role role = roleMapper.selectById(emp.getRoleId());
        if (role == null) {
            return Set.of();
        }

        List<Menu> menus = "admin".equals(role.getRoleKey())
                ? menuMapper.selectAll()
                : menuMapper.selectByRoleId(role.getId());

        return menus.stream()
                .map(Menu::getPerms)
                .filter(p -> p != null && !p.isBlank())
                .collect(Collectors.toSet());
    }
}
