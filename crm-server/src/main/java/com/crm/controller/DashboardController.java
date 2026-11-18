package com.crm.controller;

import com.crm.common.DataScope;
import com.crm.common.Result;
import com.crm.mapper.ReportMapper;
import com.crm.service.DashboardService;
import com.crm.service.DataScopeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;
    @Resource
    private ReportMapper reportMapper;
    @Resource
    private DataScopeService dataScopeService;

    /** 经营概览 */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.ok(dashboardService.overview());
    }

    /** 我的待办：待跟进线索 / 待处理工单 / 待处理退换 / 待收账款 */
    @GetMapping("/todo")
    public Result<Map<String, Object>> todo() {
        DataScope ds = dataScopeService.current();
        List<Map<String, Object>> receivables = reportMapper.receivable(ds);
        if (receivables.size() > 5) {
            receivables = receivables.subList(0, 5);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("leads", dashboardService.todoLeads(ds));
        data.put("tickets", reportMapper.todoTickets(ds));
        data.put("returns", reportMapper.todoReturns(ds));
        data.put("receivables", receivables);
        return Result.ok(data);
    }
}
