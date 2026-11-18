package com.crm.service;

import com.crm.common.BizException;
import com.crm.common.DataScope;
import com.crm.common.PageResult;
import com.crm.common.UserContext;
import com.crm.entity.Customer;
import com.crm.entity.Follow;
import com.crm.entity.Lead;
import com.crm.entity.Opportunity;
import com.crm.mapper.CustomerMapper;
import com.crm.mapper.FollowMapper;
import com.crm.mapper.LeadMapper;
import com.crm.mapper.OpportunityMapper;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FollowService {

    @Resource
    private FollowMapper followMapper;
    @Resource
    private LeadMapper leadMapper;
    @Resource
    private CustomerMapper customerMapper;
    @Resource
    private OpportunityMapper opportunityMapper;
    @Resource
    private DataScopeService dataScopeService;

    public List<Follow> list(String bizType, Long bizId) {
        checkBiz(bizType, bizId);
        return followMapper.selectByBiz(bizType, bizId);
    }

    public PageResult<Follow> page(int pageNum, int pageSize, String keyword, String bizType) {
        DataScope ds = dataScopeService.current();
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(followMapper.selectPage(keyword, bizType, ds));
    }

    public void add(Follow follow) {
        checkBiz(follow.getBizType(), follow.getBizId());

        follow.setCreateBy(UserContext.get());
        followMapper.insert(follow);

        // 跟进过就刷新线索活跃时间，免得被公海回收
        if ("lead".equals(follow.getBizType())) {
            leadMapper.touchFollow(follow.getBizId());
        }
        // 客户同理，长期不跟进会被退回客户公海
        if ("customer".equals(follow.getBizType())) {
            customerMapper.touchFollow(follow.getBizId());
        }
    }

    public void delete(Long id) {
        followMapper.deleteById(id);
    }

    /**
     * 跟进记录挂在业务数据上，读和写之前都先确认这条数据轮得到你管
     */
    private void checkBiz(String bizType, Long bizId) {
        switch (bizType) {
            case "lead" -> {
                Lead lead = leadMapper.selectById(bizId);
                if (lead == null) {
                    throw new BizException("线索不存在");
                }
                dataScopeService.check(lead.getOwnerId(), lead.getDeptId());
            }
            case "customer" -> {
                Customer customer = customerMapper.selectById(bizId);
                if (customer == null) {
                    throw new BizException("客户不存在");
                }
                dataScopeService.check(customer.getOwnerId(), customer.getDeptId());
            }
            case "opportunity" -> {
                Opportunity opportunity = opportunityMapper.selectById(bizId);
                if (opportunity == null) {
                    throw new BizException("商机不存在");
                }
                dataScopeService.check(opportunity.getOwnerId(), opportunity.getDeptId());
            }
            default -> throw new BizException("不支持的跟进类型：" + bizType);
        }
    }
}
