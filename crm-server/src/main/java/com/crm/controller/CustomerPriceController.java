package com.crm.controller;

import com.crm.common.PageResult;
import com.crm.common.RequiresPerm;
import com.crm.common.Result;
import com.crm.entity.CustomerPrice;
import com.crm.service.CustomerPriceService;
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
@RequestMapping("/api/price")
public class CustomerPriceController {

    @Resource
    private CustomerPriceService priceService;

    @GetMapping("/list")
    public Result<PageResult<CustomerPrice>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                  @RequestParam(required = false) Long customerId) {
        return Result.ok(priceService.page(pageNum, pageSize, customerId));
    }

    @RequiresPerm("crm:price:add")
    @PostMapping
    public Result<Void> add(@RequestBody CustomerPrice price) {
        priceService.add(price);
        return Result.ok();
    }

    @RequiresPerm("crm:price:edit")
    @PutMapping
    public Result<Void> update(@RequestBody CustomerPrice price) {
        priceService.update(price);
        return Result.ok();
    }

    @RequiresPerm("crm:price:remove")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        priceService.delete(id, false);
        return Result.ok();
    }

    @RequiresPerm("crm:price:remove")
    @DeleteMapping("/{id}/force")
    public Result<Void> forceRemove(@PathVariable Long id) {
        priceService.delete(id, true);
        return Result.ok();
    }
}
