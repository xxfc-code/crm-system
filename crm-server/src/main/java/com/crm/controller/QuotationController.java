package com.crm.controller;

import com.crm.common.PageResult;
import com.crm.common.RequiresPerm;
import com.crm.common.Result;
import com.crm.entity.Quotation;
import com.crm.service.QuotationService;
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
@RequestMapping("/api/quotation")
public class QuotationController {

    @Resource
    private QuotationService quotationService;

    @GetMapping("/list")
    public Result<PageResult<Quotation>> list(@RequestParam(defaultValue = "1") int pageNum,
                                              @RequestParam(defaultValue = "10") int pageSize,
                                              @RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) String status) {
        return Result.ok(quotationService.page(pageNum, pageSize, keyword, status));
    }

    @GetMapping("/{id}")
    public Result<Quotation> detail(@PathVariable Long id) {
        return Result.ok(quotationService.get(id));
    }

    @RequiresPerm("crm:quotation:add")
    @PostMapping
    public Result<Void> add(@RequestBody Quotation quotation) {
        quotationService.add(quotation);
        return Result.ok();
    }

    @RequiresPerm("crm:quotation:edit")
    @PutMapping
    public Result<Void> update(@RequestBody Quotation quotation) {
        quotationService.update(quotation);
        return Result.ok();
    }

    @RequiresPerm("crm:quotation:edit")
    @PutMapping("/{id}/status")
    public Result<Void> status(@PathVariable Long id, @RequestParam String status) {
        quotationService.updateStatus(id, status);
        return Result.ok();
    }

    /** 审批报价单 */
    @RequiresPerm("crm:quotation:edit")
    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id,
                                @RequestParam boolean approved,
                                @RequestParam(required = false) String remark) {
        quotationService.approve(id, approved, remark);
        return Result.ok();
    }

    /** 报价单确认后转订单 */
    @RequiresPerm("crm:quotation:edit")
    @PostMapping("/{id}/convert")
    public Result<Long> convert(@PathVariable Long id) {
        return Result.ok(quotationService.convertToOrder(id));
    }

    @RequiresPerm("crm:quotation:remove")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        quotationService.delete(id, false);
        return Result.ok();
    }

    @RequiresPerm("crm:quotation:remove")
    @DeleteMapping("/{id}/force")
    public Result<Void> forceRemove(@PathVariable Long id) {
        quotationService.delete(id, true);
        return Result.ok();
    }
}
