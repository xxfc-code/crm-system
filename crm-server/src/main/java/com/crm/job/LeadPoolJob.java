package com.crm.job;

import com.crm.service.CustomerService;
import com.crm.service.LeadService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LeadPoolJob {

    @Resource
    private LeadService leadService;
    @Resource
    private CustomerService customerService;

    /** 每天凌晨 2 点扫一次，超期没跟进的线索自动回公海 */
    @Scheduled(cron = "0 0 2 * * ?")
    public void recycle() {
        int rows = leadService.recycle();
        log.info("公海回收完成，{} 条线索回到公海", rows);
    }

    /** 凌晨 3 点回收长期没跟进的客户 */
    @Scheduled(cron = "0 0 3 * * ?")
    public void recycleCustomers() {
        int rows = customerService.recycle();
        log.info("客户公海回收完成，{} 个客户回到公海", rows);
    }
}
