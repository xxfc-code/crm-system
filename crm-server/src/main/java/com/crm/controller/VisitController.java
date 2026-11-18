package com.crm.controller;

import com.crm.common.PageResult;
import com.crm.common.RequiresPerm;
import com.crm.common.Result;
import com.crm.entity.Visit;
import com.crm.service.VisitService;
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
@RequestMapping("/api/visit")
public class VisitController {

    @Resource
    private VisitService visitService;

    @GetMapping("/list")
    public Result<PageResult<Visit>> list(@RequestParam(defaultValue = "1") int pageNum,
                                          @RequestParam(defaultValue = "10") int pageSize,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String way) {
        return Result.ok(visitService.page(pageNum, pageSize, keyword, way));
    }

    @RequiresPerm("crm:visit:add")
    @PostMapping
    public Result<Void> add(@RequestBody Visit visit) {
        visitService.add(visit);
        return Result.ok();
    }

    @RequiresPerm("crm:visit:edit")
    @PutMapping
    public Result<Void> update(@RequestBody Visit visit) {
        visitService.update(visit);
        return Result.ok();
    }

    @RequiresPerm("crm:visit:remove")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        visitService.delete(id, false);
        return Result.ok();
    }

    @RequiresPerm("crm:visit:remove")
    @DeleteMapping("/{id}/force")
    public Result<Void> forceRemove(@PathVariable Long id) {
        visitService.delete(id, true);
        return Result.ok();
    }
}
