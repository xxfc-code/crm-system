package com.crm.controller;

import com.crm.common.PageResult;
import com.crm.common.RequiresPerm;
import com.crm.common.Result;
import com.crm.entity.Payment;
import com.crm.service.PaymentService;
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
@RequestMapping("/api/payment")
public class PaymentController {

    @Resource
    private PaymentService paymentService;

    @GetMapping("/list")
    public Result<PageResult<Payment>> list(@RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String payType) {
        return Result.ok(paymentService.page(pageNum, pageSize, keyword, payType));
    }

    @RequiresPerm("crm:payment:add")
    @PostMapping
    public Result<Void> add(@RequestBody Payment payment) {
        paymentService.add(payment);
        return Result.ok();
    }

    @RequiresPerm("crm:payment:edit")
    @PutMapping
    public Result<Void> update(@RequestBody Payment payment) {
        paymentService.update(payment);
        return Result.ok();
    }

    @RequiresPerm("crm:payment:remove")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        paymentService.delete(id, false);
        return Result.ok();
    }

    @RequiresPerm("crm:payment:remove")
    @DeleteMapping("/{id}/force")
    public Result<Void> forceRemove(@PathVariable Long id) {
        paymentService.delete(id, true);
        return Result.ok();
    }
}
