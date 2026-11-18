package com.crm.controller;

import com.crm.common.RequiresPerm;
import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.entity.CrmOrder;
import com.crm.service.OrderService;
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
@RequestMapping("/api/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    @GetMapping("/list")
    public Result<PageResult<CrmOrder>> list(@RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "10") int pageSize,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String status) {
        return Result.ok(orderService.page(pageNum, pageSize, keyword, status));
    }

    @GetMapping("/{id}")
    public Result<CrmOrder> detail(@PathVariable Long id) {
        return Result.ok(orderService.get(id));
    }

    /** 发货、收款、退换货选订单用 */
    @GetMapping("/options")
    public Result<java.util.List<CrmOrder>> options() {
        return Result.ok(orderService.options());
    }

    @RequiresPerm("crm:order:add")
    @PostMapping
    public Result<Void> add(@RequestBody CrmOrder order) {
        orderService.add(order);
        return Result.ok();
    }

    @RequiresPerm("crm:order:edit")
    @PutMapping
    public Result<Void> update(@RequestBody CrmOrder order) {
        orderService.update(order);
        return Result.ok();
    }

    /** 从订单生成发货单 */
    public record DeliveryParam(String receiver, String phone, String address) {
    }

    /** 从订单生成收款单 */
    public record PaymentParam(java.math.BigDecimal amount, java.time.LocalDate payDate, String payType, String remark) {
    }

    @PostMapping("/{id}/delivery")
    public Result<com.crm.entity.Delivery> createDelivery(@PathVariable Long id, @RequestBody DeliveryParam param) {
        return Result.ok(orderService.createDelivery(id, param.receiver(), param.phone(), param.address()));
    }

    @PostMapping("/{id}/payment")
    public Result<com.crm.entity.Payment> createPayment(@PathVariable Long id, @RequestBody PaymentParam param) {
        return Result.ok(orderService.createPayment(id, param.amount(), param.payDate(), param.payType(), param.remark()));
    }

    @RequiresPerm("crm:order:status")
    @PutMapping("/{id}/status")
    public Result<Void> status(@PathVariable Long id, @RequestParam String status) {
        orderService.updateStatus(id, status);
        return Result.ok();
    }

    @RequiresPerm("crm:order:remove")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        orderService.delete(id, false);
        return Result.ok();
    }

    @RequiresPerm("crm:order:remove")
    @DeleteMapping("/{id}/force")
    public Result<Void> forceRemove(@PathVariable Long id) {
        orderService.delete(id, true);
        return Result.ok();
    }
}
