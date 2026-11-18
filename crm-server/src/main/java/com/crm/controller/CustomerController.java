package com.crm.controller;

import com.crm.common.RequiresPerm;
import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.entity.Customer;
import com.crm.service.CustomerService;
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
@RequestMapping("/api/customer")
public class CustomerController {

    @Resource
    private CustomerService customerService;

    @GetMapping("/list")
    public Result<PageResult<Customer>> list(@RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "10") int pageSize,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String level) {
        return Result.ok(customerService.page(pageNum, pageSize, keyword, level));
    }

    @GetMapping("/options")
    public Result<List<Customer>> options() {
        return Result.ok(customerService.options());
    }

    /** 客户公海：没有负责人的客户 */
    @GetMapping("/pool")
    public Result<PageResult<Customer>> pool(@RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "10") int pageSize,
                                             @RequestParam(required = false) String keyword) {
        return Result.ok(customerService.pool(pageNum, pageSize, keyword));
    }

    /** 公海回收天数，前端文案跟后端配置保持一致 */
    @GetMapping("/pool/days")
    public Result<Integer> poolDays() {
        return Result.ok(customerService.getPoolDays());
    }

    /** 从公海领取客户 */
    @PutMapping("/{id}/claim")
    public Result<Void> claim(@PathVariable Long id) {
        customerService.claim(id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<Customer> detail(@PathVariable Long id) {
        return Result.ok(customerService.get(id));
    }

    @RequiresPerm("crm:customer:add")
    @PostMapping
    public Result<Void> add(@RequestBody Customer customer) {
        customerService.add(customer);
        return Result.ok();
    }

    @RequiresPerm("crm:customer:edit")
    @PutMapping
    public Result<Void> update(@RequestBody Customer customer) {
        customerService.update(customer);
        return Result.ok();
    }

    @RequiresPerm("crm:customer:remove")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        customerService.delete(id, false);
        return Result.ok();
    }

    @RequiresPerm("crm:customer:remove")
    @DeleteMapping("/{id}/force")
    public Result<Void> forceRemove(@PathVariable Long id) {
        customerService.delete(id, true);
        return Result.ok();
    }
}
