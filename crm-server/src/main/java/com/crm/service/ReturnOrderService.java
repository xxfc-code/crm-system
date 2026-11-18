package com.crm.service;

import com.crm.common.BizException;
import com.crm.common.CodeUtil;
import com.crm.common.DataScope;
import com.crm.common.PageResult;
import com.crm.entity.Employee;
import com.crm.entity.OrderItem;
import com.crm.entity.Payment;
import com.crm.entity.Product;
import com.crm.entity.ReturnOrder;
import com.crm.mapper.OrderItemMapper;
import com.crm.mapper.OrderMapper;
import com.crm.mapper.PaymentMapper;
import com.crm.mapper.ProductMapper;
import com.crm.mapper.ReturnOrderMapper;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ReturnOrderService {

    @Resource
    private ReturnOrderMapper returnOrderMapper;
    @Resource
    private PaymentMapper paymentMapper;
    @Resource
    private OrderItemMapper orderItemMapper;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private OrderService orderService;
    @Resource
    private DataScopeService dataScopeService;

    public PageResult<ReturnOrder> page(int pageNum, int pageSize, String keyword, String status) {
        DataScope ds = dataScopeService.current();
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(returnOrderMapper.selectList(keyword, status, ds));
    }

    public void add(ReturnOrder order) {
        if (order.getOwnerId() == null) {
            Employee me = dataScopeService.me();
            order.setOwnerId(me.getId());
            order.setDeptId(me.getDeptId());
        }
        returnOrderMapper.insert(order);
        returnOrderMapper.updateNo(order.getId(), CodeUtil.gen("TH", order.getId()));
    }

    public void update(ReturnOrder order) {
        load(order.getId());
        returnOrderMapper.update(order);
    }

    /**
     * 处理完就冲销：写一条负数收款，应收自然减少，订单状态跟着重算
     */
    public void updateStatus(Long id, String status) {
        ReturnOrder order = load(id);
        LocalDate handleDate = "已处理".equals(status) ? LocalDate.now() : null;

        if ("已处理".equals(status) && order.getOrderId() != null) {
            // 退货回补库存
            if ("退货".equals(order.getReturnType())) {
                for (OrderItem item : orderItemMapper.selectByOrder(order.getOrderId())) {
                    productMapper.restoreStock(item.getProductId(), item.getQty());
                }
            }

            if (paymentMapper.countByReturn(id) == 0) {
                Payment back = new Payment();
                back.setOrderId(order.getOrderId());
                back.setReturnId(id);
                back.setCustomerId(order.getCustomerId());
                back.setOwnerId(order.getOwnerId());
                back.setDeptId(order.getDeptId());
                back.setAmount(order.getAmount().negate());
                back.setPayDate(handleDate);
                back.setPayType("退换货冲销");
                back.setRemark("退换货单 " + order.getReturnNo() + " 冲销");
                paymentMapper.insert(back);
                paymentMapper.updateNo(back.getId(), CodeUtil.gen("SK", back.getId()));
                orderService.refreshStatus(order.getOrderId());
            }
        }

        returnOrderMapper.updateStatus(id, status, handleDate);
    }

    public void delete(Long id, boolean force) {
        load(id);
        if (force) {
            returnOrderMapper.deleteForce(id);
        } else {
            returnOrderMapper.deleteLogic(id);
        }
    }

    private ReturnOrder load(Long id) {
        ReturnOrder order = returnOrderMapper.selectById(id);
        if (order == null) {
            throw new BizException("退换货单不存在");
        }
        dataScopeService.check(order.getOwnerId(), order.getDeptId());
        return order;
    }
}
