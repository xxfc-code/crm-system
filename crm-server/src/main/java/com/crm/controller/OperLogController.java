package com.crm.controller;

import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.entity.OperLog;
import com.crm.mapper.OperLogMapper;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/operLog")
public class OperLogController {

    @Resource
    private OperLogMapper operLogMapper;

    @GetMapping("/list")
    public Result<PageResult<OperLog>> list(@RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String module) {
        PageHelper.startPage(pageNum, pageSize);
        return Result.ok(PageResult.of(operLogMapper.selectList(keyword, module)));
    }
}
