package com.crm.service;

import com.crm.common.BizException;
import com.crm.common.CodeUtil;
import com.crm.common.DataScope;
import com.crm.common.PageResult;
import com.crm.entity.Delivery;
import com.crm.entity.Employee;
import com.crm.mapper.DeliveryMapper;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class DeliveryService {

    @Resource
    private DeliveryMapper deliveryMapper;
    @Resource
    private DataScopeService dataScopeService;
    @Resource
    private OrderService orderService;

    public PageResult<Delivery> page(int pageNum, int pageSize, String keyword, String status) {
        DataScope ds = dataScopeService.current();
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(deliveryMapper.selectList(keyword, status, ds));
    }

    public void add(Delivery delivery) {
        if (delivery.getOwnerId() == null) {
            Employee me = dataScopeService.me();
            delivery.setOwnerId(me.getId());
            delivery.setDeptId(me.getDeptId());
        }
        deliveryMapper.insert(delivery);
        deliveryMapper.updateNo(delivery.getId(), CodeUtil.gen("FH", delivery.getId()));
        // 发货会影响订单状态
        if (delivery.getOrderId() != null) {
            orderService.refreshStatus(delivery.getOrderId());
        }
    }

    public void update(Delivery delivery) {
        load(delivery.getId());
        deliveryMapper.update(delivery);
    }

    public void updateStatus(Long id, String status) {
        load(id);
        deliveryMapper.updateStatus(id, status);
    }

    public void delete(Long id, boolean force) {
        load(id);
        if (force) {
            deliveryMapper.deleteForce(id);
        } else {
            deliveryMapper.deleteLogic(id);
        }
    }

    private Delivery load(Long id) {
        Delivery delivery = deliveryMapper.selectById(id);
        if (delivery == null) {
            throw new BizException("发货记录不存在");
        }
        dataScopeService.check(delivery.getOwnerId(), delivery.getDeptId());
        return delivery;
    }
}
