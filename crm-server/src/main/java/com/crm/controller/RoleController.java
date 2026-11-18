package com.crm.controller;

import com.crm.common.RequiresPerm;
import com.crm.common.Result;
import com.crm.entity.Role;
import com.crm.service.RoleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Resource
    private RoleService roleService;

    @GetMapping("/list")
    public Result<List<Role>> list() {
        return Result.ok(roleService.list());
    }

    @RequiresPerm("crm:role:add")
    @PostMapping
    public Result<Void> add(@RequestBody Role role) {
        roleService.add(role);
        return Result.ok();
    }

    @RequiresPerm("crm:role:edit")
    @PutMapping
    public Result<Void> update(@RequestBody Role role) {
        roleService.update(role);
        return Result.ok();
    }

    @RequiresPerm("crm:role:remove")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        roleService.delete(id, false);
        return Result.ok();
    }

    @RequiresPerm("crm:role:remove")
    @DeleteMapping("/{id}/force")
    public Result<Void> forceRemove(@PathVariable Long id) {
        roleService.delete(id, true);
        return Result.ok();
    }

    /** 该角色已勾选的菜单ID，授权弹窗回显用 */
    @GetMapping("/{id}/menus")
    public Result<List<Long>> menuIds(@PathVariable Long id) {
        return Result.ok(roleService.menuIds(id));
    }

    /** 保存授权，body 直接传 menuId 数组 */
    @RequiresPerm("crm:role:auth")
    @PutMapping("/{id}/menus")
    public Result<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
        return Result.ok();
    }
}
