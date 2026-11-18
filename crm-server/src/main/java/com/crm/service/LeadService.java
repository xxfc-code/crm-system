package com.crm.service;

import com.crm.common.BizException;
import com.crm.common.CodeUtil;
import com.crm.common.DataScope;
import com.crm.common.PageResult;
import com.crm.common.UserContext;
import com.crm.entity.Customer;
import com.crm.entity.Employee;
import com.crm.entity.Lead;
import com.crm.mapper.CustomerMapper;
import com.crm.mapper.EmployeeMapper;
import com.crm.mapper.LeadMapper;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LeadService {

    @Resource
    private LeadMapper leadMapper;
    @Resource
    private CustomerMapper customerMapper;
    @Resource
    private EmployeeMapper employeeMapper;
    @Resource
    private DataScopeService dataScopeService;

    /** 多少天没跟进就掉公海，可在 application.yml 调 */
    @Value("${crm.lead.pool-days:15}")
    private int poolDays;

    public PageResult<Lead> page(int pageNum, int pageSize, String keyword, Integer status, String source) {
        // 数据范围要先算，它内部会查库；必须在 startPage 之前，否则那次查询会把分页参数吃掉
        DataScope ds = dataScopeService.current();
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(leadMapper.selectList(keyword, status, source, ds));
    }

    public PageResult<Lead> pool(int pageNum, int pageSize, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(leadMapper.selectPool(keyword));
    }

    public Lead get(Long id) {
        return load(id);
    }

    public void add(Lead lead) {
        // 不指定负责人就默认归自己，否则隔离后自己都看不见
        if (lead.getOwnerId() == null) {
            Employee me = employeeMapper.selectById(UserContext.get());
            lead.setOwnerId(me.getId());
            lead.setDeptId(me.getDeptId());
        }
        leadMapper.insert(lead);
        leadMapper.updateNo(lead.getId(), CodeUtil.gen("LD", lead.getId()));
    }

    public void update(Lead lead) {
        checkWritable(load(lead.getId()));
        leadMapper.update(lead);
    }

    public void updateStatus(Long id, Integer status) {
        load(id);
        leadMapper.updateStatus(id, status);
    }

    /** 从公海领一条，抢不到会提示 */
    public void claim(Long id) {
        Employee me = employeeMapper.selectById(UserContext.get());
        int rows = leadMapper.claim(id, me.getId(), me.getDeptId());
        if (rows == 0) {
            throw new BizException("这条线索已经有人负责了，刷新看看");
        }
    }

    /** 扫描超期线索收回公海，定时任务和手动触发共用 */
    public int recycle() {
        return leadMapper.recycleToPool(LocalDateTime.now().minusDays(poolDays));
    }

    public int getPoolDays() {
        return poolDays;
    }

    /**
     * 线索转客户：建客户 + 回写线索状态，两步必须一起成功
     */
    @Transactional
    public Long convert(Long id) {
        Lead lead = load(id);
        if (lead.getOwnerId() == null) {
            throw new BizException("公海线索请先领取再转化");
        }
        if (lead.getCustomerId() != null) {
            throw new BizException("该线索已经转化过了");
        }
        if (lead.getStatus() != null && lead.getStatus() == 3) {
            throw new BizException("已废弃的线索不能转化，请先把状态改回来");
        }

        Customer customer = new Customer();
        boolean hasCompany = lead.getCompany() != null && !lead.getCompany().isBlank();
        customer.setCustomerName(hasCompany ? lead.getCompany() : lead.getLeadName());
        customer.setContact(lead.getLeadName());
        customer.setPhone(lead.getPhone());
        customer.setSource(lead.getSource());
        customer.setOwnerId(lead.getOwnerId());
        customer.setDeptId(lead.getDeptId());
        customer.setLeadId(lead.getId());

        customerMapper.insert(customer);
        customerMapper.updateNo(customer.getId(), CodeUtil.gen("KH", customer.getId()));
        leadMapper.updateConverted(id, customer.getId());
        return customer.getId();
    }

    public void delete(Long id, boolean force) {
        checkWritable(load(id));
        if (force) {
            leadMapper.deleteForce(id);
        } else {
            leadMapper.deleteLogic(id);
        }
    }

    /**
     * 公海里的线索谁都能看，但只有管理员能直接改或删，其他人得先领取
     */
    private void checkWritable(Lead lead) {
        if (lead.getOwnerId() == null && !dataScopeService.current().isAll()) {
            throw new BizException("这是公海线索，请先领取再操作");
        }
    }

    /**
     * 取出来并做归属校验，越权在这里就拦住，后面的方法不用再判断
     */
    private Lead load(Long id) {
        Lead lead = leadMapper.selectById(id);
        if (lead == null) {
            throw new BizException("线索不存在");
        }
        dataScopeService.check(lead.getOwnerId(), lead.getDeptId());
        return lead;
    }
}
