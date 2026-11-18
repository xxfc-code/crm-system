package com.crm.controller;

import com.crm.common.PageResult;
import com.crm.common.RequiresPerm;
import com.crm.common.Result;
import com.crm.entity.ReturnOrder;
import com.crm.service.ReturnOrderService;
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

@RestController
@RequestMapping("/api/return")
public class ReturnOrderController {

    @Resource
    private ReturnOrderService returnOrderService;

    @GetMapping("/list")
    public Result<PageResult<ReturnOrder>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                @RequestParam(defaultValue = "10") int pageSize,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String status) {
        return Result.ok(returnOrderService.page(pageNum, pageSize, keyword, status));
    }

    @RequiresPerm("crm:return:add")
    @PostMapping
    public Result<Void> add(@RequestBody ReturnOrder order) {
        returnOrderService.add(order);
        return Result.ok();
    }

    @RequiresPerm("crm:return:edit")
    @PutMapping
    public Result<Void> update(@RequestBody ReturnOrder order) {
        returnOrderService.update(order);
        return Result.ok();
    }

    @RequiresPerm("crm:return:edit")
    @PutMapping("/{id}/status")
    public Result<Void> status(@PathVariable Long id, @RequestParam String status) {
        returnOrderService.updateStatus(id, status);
        return Result.ok();
    }

    @RequiresPerm("crm:return:remove")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        returnOrderService.delete(id, false);
        return Result.ok();
    }

    @RequiresPerm("crm:return:remove")
    @DeleteMapping("/{id}/force")
    public Result<Void> forceRemove(@PathVariable Long id) {
        returnOrderService.delete(id, true);
        return Result.ok();
    }
}
