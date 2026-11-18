package com.crm.controller;

import com.crm.common.RequiresPerm;
import com.crm.common.Result;
import com.crm.entity.Menu;
import com.crm.service.MenuService;
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
@RequestMapping("/api/menu")
public class MenuController {

    @Resource
    private MenuService menuService;

    /** 全量菜单树，角色授权页的权限勾选列表 */
    @GetMapping("/tree")
    public Result<List<Menu>> tree() {
        return Result.ok(menuService.allTree());
    }

    /** 权限管理页，包含停用的菜单 */
    @GetMapping("/manage")
    public Result<List<Menu>> manage() {
        return Result.ok(menuService.manageTree());
    }

    @RequiresPerm("crm:menu:add")
    @PostMapping
    public Result<Void> add(@RequestBody Menu menu) {
        menuService.add(menu);
        return Result.ok();
    }

    @RequiresPerm("crm:menu:edit")
    @PutMapping
    public Result<Void> update(@RequestBody Menu menu) {
        menuService.update(menu);
        return Result.ok();
    }

    @RequiresPerm("crm:menu:remove")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        menuService.delete(id);
        return Result.ok();
    }
}
