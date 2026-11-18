package com.crm.controller;

import com.crm.common.PageResult;
import com.crm.common.RequiresPerm;
import com.crm.common.Result;
import com.crm.entity.Delivery;
import com.crm.service.DeliveryService;
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
@RequestMapping("/api/delivery")
public class DeliveryController {

    @Resource
    private DeliveryService deliveryService;

    @GetMapping("/list")
    public Result<PageResult<Delivery>> list(@RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "10") int pageSize,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String status) {
        return Result.ok(deliveryService.page(pageNum, pageSize, keyword, status));
    }

    @RequiresPerm("crm:delivery:add")
    @PostMapping
    public Result<Void> add(@RequestBody Delivery delivery) {
        deliveryService.add(delivery);
        return Result.ok();
    }

    @RequiresPerm("crm:delivery:edit")
    @PutMapping
    public Result<Void> update(@RequestBody Delivery delivery) {
        deliveryService.update(delivery);
        return Result.ok();
    }

    @RequiresPerm("crm:delivery:edit")
    @PutMapping("/{id}/status")
    public Result<Void> status(@PathVariable Long id, @RequestParam String status) {
        deliveryService.updateStatus(id, status);
        return Result.ok();
    }

    @RequiresPerm("crm:delivery:remove")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        deliveryService.delete(id, false);
        return Result.ok();
    }

    @RequiresPerm("crm:delivery:remove")
    @DeleteMapping("/{id}/force")
    public Result<Void> forceRemove(@PathVariable Long id) {
        deliveryService.delete(id, true);
        return Result.ok();
    }
}
