package com.crm.controller;

import com.crm.common.PageResult;
import com.crm.common.RequiresPerm;
import com.crm.common.Result;
import com.crm.entity.Employee;
import com.crm.service.EmployeeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    @Resource
    private EmployeeService employeeService;

    @GetMapping("/list")
    public Result<PageResult<Employee>> list(@RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "10") int pageSize,
                                             @RequestParam(required = false) Long deptId,
                                             @RequestParam(required = false) String keyword) {
        return Result.ok(employeeService.page(pageNum, pageSize, deptId, keyword));
    }

    /** 表单里的负责人下拉 */
    @GetMapping("/options")
    public Result<List<Employee>> options() {
        return Result.ok(employeeService.options());
    }

    @RequiresPerm("crm:employee:add")
    @PostMapping
    public Result<Void> add(@RequestBody Employee employee) {
        employeeService.add(employee);
        return Result.ok();
    }

    @RequiresPerm("crm:employee:edit")
    @PutMapping
    public Result<Void> update(@RequestBody Employee employee) {
        employeeService.update(employee);
        return Result.ok();
    }

    @RequiresPerm("crm:employee:resetPwd")
    @PutMapping("/{id}/resetPwd")
    public Result<Void> resetPassword(@PathVariable Long id) {
        employeeService.resetPassword(id);
        return Result.ok();
    }

    @RequiresPerm("crm:employee:remove")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        employeeService.delete(id, false);
        return Result.ok();
    }

    @RequiresPerm("crm:employee:remove")
    @DeleteMapping("/{id}/force")
    public Result<Void> forceRemove(@PathVariable Long id) {
        employeeService.delete(id, true);
        return Result.ok();
    }
}
