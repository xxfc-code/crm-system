package com.crm.controller;

import com.crm.common.RequiresPerm;
import com.crm.common.Result;
import com.crm.entity.Dept;
import com.crm.service.DeptService;
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
@RequestMapping("/api/dept")
public class DeptController {

    @Resource
    private DeptService deptService;

    @GetMapping("/tree")
    public Result<List<Dept>> tree() {
        return Result.ok(deptService.tree());
    }

    @GetMapping("/{id}")
    public Result<Dept> detail(@PathVariable Long id) {
        return Result.ok(deptService.get(id));
    }

    @RequiresPerm("crm:dept:add")
    @PostMapping
    public Result<Void> add(@RequestBody Dept dept) {
        deptService.add(dept);
        return Result.ok();
    }

    @RequiresPerm("crm:dept:edit")
    @PutMapping
    public Result<Void> update(@RequestBody Dept dept) {
        deptService.update(dept);
        return Result.ok();
    }

    /** 逻辑删除 */
    @RequiresPerm("crm:dept:remove")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        deptService.delete(id, false);
        return Result.ok();
    }

    /** 物理删除，管理员用 */
    @RequiresPerm("crm:dept:remove")
    @DeleteMapping("/{id}/force")
    public Result<Void> forceRemove(@PathVariable Long id) {
        deptService.delete(id, true);
        return Result.ok();
    }
}
