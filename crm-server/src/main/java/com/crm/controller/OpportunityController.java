package com.crm.controller;

import com.crm.common.RequiresPerm;
import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.entity.Opportunity;
import com.crm.service.OpportunityService;
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
@RequestMapping("/api/opportunity")
public class OpportunityController {

    @Resource
    private OpportunityService opportunityService;

    @GetMapping("/list")
    public Result<PageResult<Opportunity>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                @RequestParam(defaultValue = "10") int pageSize,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String stage,
                                                @RequestParam(required = false) Long customerId) {
        return Result.ok(opportunityService.page(pageNum, pageSize, keyword, stage, customerId));
    }

    @GetMapping("/options")
    public Result<List<Opportunity>> options(@RequestParam(required = false) Long customerId) {
        return Result.ok(opportunityService.options(customerId));
    }

    @GetMapping("/{id}")
    public Result<Opportunity> detail(@PathVariable Long id) {
        return Result.ok(opportunityService.get(id));
    }

    @RequiresPerm("crm:opportunity:add")
    @PostMapping
    public Result<Void> add(@RequestBody Opportunity opportunity) {
        opportunityService.add(opportunity);
        return Result.ok();
    }

    @RequiresPerm("crm:opportunity:edit")
    @PutMapping
    public Result<Void> update(@RequestBody Opportunity opportunity) {
        opportunityService.update(opportunity);
        return Result.ok();
    }

    @RequiresPerm("crm:opportunity:stage")
    @PutMapping("/{id}/stage")
    public Result<Void> stage(@PathVariable Long id,
                              @RequestParam String stage,
                              @RequestParam(required = false) Integer winRate) {
        opportunityService.updateStage(id, stage, winRate);
        return Result.ok();
    }

    @RequiresPerm("crm:opportunity:remove")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        opportunityService.delete(id, false);
        return Result.ok();
    }

    @RequiresPerm("crm:opportunity:remove")
    @DeleteMapping("/{id}/force")
    public Result<Void> forceRemove(@PathVariable Long id) {
        opportunityService.delete(id, true);
        return Result.ok();
    }
}
