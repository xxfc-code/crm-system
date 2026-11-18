package com.crm.service;

import com.crm.common.BizException;
import com.crm.common.CodeUtil;
import com.crm.common.DataScope;
import com.crm.common.PageResult;
import com.crm.common.UserContext;
import com.crm.entity.Employee;
import com.crm.entity.Opportunity;
import com.crm.entity.OpportunityStageLog;
import com.crm.mapper.OpportunityMapper;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpportunityService {

    @Resource
    private OpportunityMapper opportunityMapper;
    @Resource
    private DataScopeService dataScopeService;

    public PageResult<Opportunity> page(int pageNum, int pageSize, String keyword, String stage, Long customerId) {
        // 先算数据范围（内部查库），再开分页
        DataScope ds = dataScopeService.current();
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(opportunityMapper.selectList(keyword, stage, customerId, ds));
    }

    /** 订单表单选商机用 */
    public List<Opportunity> options(Long customerId) {
        return opportunityMapper.selectList(null, null, customerId, dataScopeService.current());
    }

    public Opportunity get(Long id) {
        Opportunity opportunity = load(id);
        opportunity.setStageLogs(opportunityMapper.selectStageLogs(id));
        return opportunity;
    }

    public void add(Opportunity opportunity) {
        if (opportunity.getOwnerId() == null) {
            Employee me = dataScopeService.me();
            opportunity.setOwnerId(me.getId());
            opportunity.setDeptId(me.getDeptId());
        }
        opportunityMapper.insert(opportunity);
        opportunityMapper.updateNo(opportunity.getId(), CodeUtil.gen("SJ", opportunity.getId()));
    }

    public void update(Opportunity opportunity) {
        load(opportunity.getId());
        opportunityMapper.update(opportunity);
    }

    /** 推进阶段：留一条流转记录，赢率随阶段带出 */
    public void updateStage(Long id, String stage, Integer winRate) {
        Opportunity opportunity = load(id);
        int rate = winRate != null ? winRate : opportunity.getWinRate();

        opportunityMapper.updateStage(id, stage, rate);

        OpportunityStageLog log = new OpportunityStageLog();
        log.setOpportunityId(id);
        log.setFromStage(opportunity.getStage());
        log.setToStage(stage);
        log.setWinRate(rate);
        log.setOperatorId(UserContext.get());
        opportunityMapper.insertStageLog(log);
    }

    public void delete(Long id, boolean force) {
        load(id);
        if (force) {
            opportunityMapper.deleteForce(id);
        } else {
            opportunityMapper.deleteLogic(id);
        }
    }

    private Opportunity load(Long id) {
        Opportunity opportunity = opportunityMapper.selectById(id);
        if (opportunity == null) {
            throw new BizException("商机不存在");
        }
        dataScopeService.check(opportunity.getOwnerId(), opportunity.getDeptId());
        return opportunity;
    }
}
