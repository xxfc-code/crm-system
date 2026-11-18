package com.crm.service;

import com.crm.common.BizException;
import com.crm.common.CodeUtil;
import com.crm.common.DataScope;
import com.crm.common.PageResult;
import com.crm.entity.Employee;
import com.crm.entity.Payment;
import com.crm.mapper.PaymentMapper;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Resource
    private PaymentMapper paymentMapper;
    @Resource
    private DataScopeService dataScopeService;
    @Resource
    private OrderService orderService;

    public PageResult<Payment> page(int pageNum, int pageSize, String keyword, String payType) {
        DataScope ds = dataScopeService.current();
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(paymentMapper.selectList(keyword, payType, ds));
    }

    public void add(Payment payment) {
        if (payment.getOwnerId() == null) {
            Employee me = dataScopeService.me();
            payment.setOwnerId(me.getId());
            payment.setDeptId(me.getDeptId());
        }
        paymentMapper.insert(payment);
        paymentMapper.updateNo(payment.getId(), CodeUtil.gen("SK", payment.getId()));
        // 收款会影响订单状态
        if (payment.getOrderId() != null) {
            orderService.refreshStatus(payment.getOrderId());
        }
    }

    public void update(Payment payment) {
        load(payment.getId());
        paymentMapper.update(payment);
    }

    public void delete(Long id, boolean force) {
        load(id);
        if (force) {
            paymentMapper.deleteForce(id);
        } else {
            paymentMapper.deleteLogic(id);
        }
    }

    private Payment load(Long id) {
        Payment payment = paymentMapper.selectById(id);
        if (payment == null) {
            throw new BizException("收款记录不存在");
        }
        dataScopeService.check(payment.getOwnerId(), payment.getDeptId());
        return payment;
    }
}
