package com.crm.service;

import com.crm.common.BizException;
import com.crm.common.CodeUtil;
import com.crm.common.DataScope;
import com.crm.common.PageResult;
import com.crm.entity.Customer;
import com.crm.entity.Employee;
import com.crm.mapper.CustomerMapper;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerService {

    /** 多少天没跟进就把客户退回公海 */
    @Value("${crm.customer.pool-days:60}")
    private int poolDays;

    @Resource
    private CustomerMapper customerMapper;
    @Resource
    private DataScopeService dataScopeService;

    public PageResult<Customer> page(int pageNum, int pageSize, String keyword, String level) {
        // 先算数据范围（内部查库），再开分页
        DataScope ds = dataScopeService.current();
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(customerMapper.selectList(keyword, level, ds));
    }

    /** 下拉选项用，不分页 */
    public List<Customer> options() {
        return customerMapper.selectList(null, null, dataScopeService.current());
    }

    /** 客户公海 */
    public PageResult<Customer> pool(int pageNum, int pageSize, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(customerMapper.selectPool(keyword));
    }

    /** 从公海领取客户 */
    public void claim(Long id) {
        Employee me = dataScopeService.me();
        int rows = customerMapper.claim(id, me.getId(), me.getDeptId());
        if (rows == 0) {
            throw new BizException("这条客户已经有人领取了，刷新看看");
        }
    }

    /** 长期没跟进的客户释放回公海 */
    public int recycle() {
        return customerMapper.recycleToPool(LocalDateTime.now().minusDays(poolDays));
    }

    public int getPoolDays() {
        return poolDays;
    }

    public Customer get(Long id) {
        return load(id);
    }

    public void add(Customer customer) {
        // 没指定负责人就归自己，否则隔离后新建的客户自己都看不见
        if (customer.getOwnerId() == null) {
            Employee me = dataScopeService.me();
            customer.setOwnerId(me.getId());
            customer.setDeptId(me.getDeptId());
        }
        customerMapper.insert(customer);
        customerMapper.updateNo(customer.getId(), CodeUtil.gen("KH", customer.getId()));
    }

    public void update(Customer customer) {
        load(customer.getId());
        customerMapper.update(customer);
    }

    public void delete(Long id, boolean force) {
        load(id);
        if (force) {
            customerMapper.deleteForce(id);
        } else {
            customerMapper.deleteLogic(id);
        }
    }

    private Customer load(Long id) {
        Customer customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new BizException("客户不存在");
        }
        dataScopeService.check(customer.getOwnerId(), customer.getDeptId());
        return customer;
    }
}
