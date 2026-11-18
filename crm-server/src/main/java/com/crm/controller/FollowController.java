package com.crm.controller;

import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.entity.Follow;
import com.crm.service.FollowService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    @Resource
    private FollowService followService;

    @GetMapping("/list")
    public Result<List<Follow>> list(@RequestParam String bizType, @RequestParam Long bizId) {
        return Result.ok(followService.list(bizType, bizId));
    }

    /** 跟进记录页，全局分页 */
    @GetMapping("/page")
    public Result<PageResult<Follow>> page(@RequestParam(defaultValue = "1") int pageNum,
                                           @RequestParam(defaultValue = "10") int pageSize,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String bizType) {
        return Result.ok(followService.page(pageNum, pageSize, keyword, bizType));
    }

    @PostMapping
    public Result<Void> add(@RequestBody Follow follow) {
        followService.add(follow);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        followService.delete(id);
        return Result.ok();
    }
}
