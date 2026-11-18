package com.crm.controller;

import com.crm.common.RequiresPerm;
import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.entity.Lead;
import com.crm.service.LeadService;
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
@RequestMapping("/api/lead")
public class LeadController {

    @Resource
    private LeadService leadService;

    @GetMapping("/list")
    public Result<PageResult<Lead>> list(@RequestParam(defaultValue = "1") int pageNum,
                                         @RequestParam(defaultValue = "10") int pageSize,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Integer status,
                                         @RequestParam(required = false) String source) {
        return Result.ok(leadService.page(pageNum, pageSize, keyword, status, source));
    }

    @GetMapping("/{id}")
    public Result<Lead> detail(@PathVariable Long id) {
        return Result.ok(leadService.get(id));
    }

    /** 公海池：无负责人的线索 */
    @GetMapping("/pool")
    public Result<PageResult<Lead>> pool(@RequestParam(defaultValue = "1") int pageNum,
                                         @RequestParam(defaultValue = "10") int pageSize,
                                         @RequestParam(required = false) String keyword) {
        return Result.ok(leadService.pool(pageNum, pageSize, keyword));
    }

    /** 公海回收天数，前端文案要跟后端配置一致 */
    @GetMapping("/pool/days")
    public Result<Integer> poolDays() {
        return Result.ok(leadService.getPoolDays());
    }

    /** 从公海领一条 */
    @RequiresPerm("crm:lead:edit")
    @PutMapping("/{id}/claim")
    public Result<Void> claim(@PathVariable Long id) {
        leadService.claim(id);
        return Result.ok();
    }

    /** 手动触发回收，正常由定时任务每天跑 */
    @RequiresPerm("crm:lead:edit")
    @PostMapping("/pool/recycle")
    public Result<Integer> recycle() {
        return Result.ok(leadService.recycle());
    }

    @RequiresPerm("crm:lead:add")
    @PostMapping
    public Result<Void> add(@RequestBody Lead lead) {
        leadService.add(lead);
        return Result.ok();
    }

    @RequiresPerm("crm:lead:edit")
    @PutMapping
    public Result<Void> update(@RequestBody Lead lead) {
        leadService.update(lead);
        return Result.ok();
    }

    @RequiresPerm("crm:lead:edit")
    @PutMapping("/{id}/status")
    public Result<Void> status(@PathVariable Long id, @RequestParam Integer status) {
        leadService.updateStatus(id, status);
        return Result.ok();
    }

    /** 转客户，返回新客户ID */
    @RequiresPerm("crm:lead:convert")
    @PutMapping("/{id}/convert")
    public Result<Long> convert(@PathVariable Long id) {
        return Result.ok(leadService.convert(id));
    }

    @RequiresPerm("crm:lead:remove")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        leadService.delete(id, false);
        return Result.ok();
    }

    @RequiresPerm("crm:lead:remove")
    @DeleteMapping("/{id}/force")
    public Result<Void> forceRemove(@PathVariable Long id) {
        leadService.delete(id, true);
        return Result.ok();
    }
}
