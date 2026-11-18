package com.crm.service;

import com.crm.common.DataScope;
import com.crm.entity.Lead;
import com.crm.mapper.CustomerMapper;
import com.crm.mapper.DashboardMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Resource
    private DashboardMapper dashboardMapper;
    @Resource
    private CustomerMapper customerMapper;
    @Resource
    private DataScopeService dataScopeService;

    public Map<String, Object> overview() {
        DataScope ds = dataScopeService.current();

        Map<String, Object> data = new HashMap<>();
        data.put("leadNew", dashboardMapper.countLeadNew(ds));
        data.put("customerTotal", dashboardMapper.countCustomer(ds));
        data.put("oppAmount", dashboardMapper.sumOppAmount(ds));
        data.put("orderPaid", dashboardMapper.sumOrderPaid(ds));
        data.put("funnel", dashboardMapper.funnel(ds));
        data.put("sources", dashboardMapper.sources(ds));
        data.put("todo", dashboardMapper.todo(ds));
        data.put("recent", dashboardMapper.recent(ds));
        data.put("keyCustomers", customerMapper.selectKeyCustomers(ds));
        return data;
    }

    /** 我的待办页用 */
    public List<Lead> todoLeads(DataScope ds) {
        return dashboardMapper.todo(ds);
    }
}
