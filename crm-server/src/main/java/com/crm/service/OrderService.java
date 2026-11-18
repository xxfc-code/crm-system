package com.crm.service;

import com.crm.common.BizException;
import com.crm.common.CodeUtil;
import com.crm.common.DataScope;
import com.crm.common.PageResult;
import com.crm.entity.CrmOrder;
import com.crm.entity.Delivery;
import com.crm.entity.Employee;
import com.crm.entity.OrderItem;
import com.crm.entity.Payment;
import com.crm.entity.Product;
import com.crm.mapper.DeliveryMapper;
import com.crm.mapper.OrderItemMapper;
import com.crm.mapper.OrderMapper;
import com.crm.mapper.PaymentMapper;
import com.crm.mapper.ProductMapper;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class OrderService {

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderItemMapper orderItemMapper;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private PaymentMapper paymentMapper;
    @Resource
    private DeliveryMapper deliveryMapper;
    @Resource
    private DataScopeService dataScopeService;

    public PageResult<CrmOrder> page(int pageNum, int pageSize, String keyword, String status) {
        // 先算数据范围（内部查库），再开分页
        DataScope ds = dataScopeService.current();
        PageHelper.startPage(pageNum, pageSize);
        List<CrmOrder> list = orderMapper.selectList(keyword, status, ds);
        list.forEach(o -> o.setGrossProfit(o.getAmount().subtract(costOf(o))));
        return PageResult.of(list);
    }

    /** 下拉选项，不分页 */
    public List<CrmOrder> options() {
        return orderMapper.selectList(null, null, dataScopeService.current());
    }

    public CrmOrder get(Long id) {
        CrmOrder order = load(id);
        order.setItems(orderItemMapper.selectByOrder(id));
        order.setGrossProfit(order.getAmount().subtract(costOf(order)));
        return order;
    }

    @Transactional
    public void add(CrmOrder order) {
        if (order.getOwnerId() == null) {
            Employee me = dataScopeService.me();
            order.setOwnerId(me.getId());
            order.setDeptId(me.getDeptId());
        }
        order.setAmount(prepareItems(order.getItems()));
        order.setStatus("待付款");
        orderMapper.insert(order);
        orderMapper.updateNo(order.getId(), CodeUtil.gen("DD", order.getId()));
        saveItems(order.getId(), order.getItems());
    }

    @Transactional
    public void update(CrmOrder order) {
        load(order.getId());
        order.setAmount(prepareItems(order.getItems()));
        orderMapper.update(order);
        saveItems(order.getId(), order.getItems());
        refreshStatus(order.getId());
    }

    /**
     * 状态由发货和收款自动推导，唯一例外是「已取消」可以手动置
     */
    public void updateStatus(Long id, String status) {
        load(id);
        if ("已取消".equals(status)) {
            orderMapper.updateStatus(id, status);
            return;
        }
        if ("待付款".equals(status)) {
            orderMapper.updateStatus(id, status);
            refreshStatus(id);
            return;
        }
        throw new BizException("订单状态由发货和收款自动推导，只能手动取消或恢复订单");
    }

    @Transactional
    public void delete(Long id, boolean force) {
        load(id);
        orderItemMapper.deleteByOrder(id);
        if (force) {
            orderMapper.deleteForce(id);
        } else {
            orderMapper.deleteLogic(id);
        }
    }

    /** 从订单生成发货单 */
    @Transactional
    public Delivery createDelivery(Long orderId, String receiver, String phone, String address) {
        CrmOrder order = load(orderId);
        if ("已取消".equals(order.getStatus())) {
            throw new BizException("已取消的订单不能发货");
        }

        // 出库扣库存，库存不够就整单回滚
        for (OrderItem item : orderItemMapper.selectByOrder(orderId)) {
            int rows = productMapper.deductStock(item.getProductId(), item.getQty());
            if (rows == 0) {
                throw new BizException("产品「" + item.getProductName() + "」库存不足，无法发货");
            }
        }

        Delivery delivery = new Delivery();
        delivery.setOrderId(orderId);
        delivery.setCustomerId(order.getCustomerId());
        delivery.setOwnerId(order.getOwnerId());
        delivery.setDeptId(order.getDeptId());
        delivery.setStatus("待发货");
        delivery.setReceiver(receiver);
        delivery.setPhone(phone);
        delivery.setAddress(address);
        deliveryMapper.insert(delivery);
        deliveryMapper.updateNo(delivery.getId(), CodeUtil.gen("FH", delivery.getId()));
        refreshStatus(orderId);
        return delivery;
    }

    /** 从订单生成收款单，收款额不能超过未收余额 */
    public Payment createPayment(Long orderId, BigDecimal amount, LocalDate payDate, String payType, String remark) {
        CrmOrder order = load(orderId);
        if ("已取消".equals(order.getStatus())) {
            throw new BizException("已取消的订单不能收款");
        }
        BigDecimal remain = order.getAmount().subtract(paymentMapper.sumByOrder(orderId));
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("收款金额要大于 0");
        }
        if (amount.compareTo(remain) > 0) {
            throw new BizException("收款金额超过未收余额（剩余 " + remain.stripTrailingZeros().toPlainString() + "）");
        }

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setCustomerId(order.getCustomerId());
        payment.setOwnerId(order.getOwnerId());
        payment.setDeptId(order.getDeptId());
        payment.setAmount(amount);
        payment.setPayDate(payDate == null ? LocalDate.now() : payDate);
        payment.setPayType(payType);
        payment.setRemark(remark);
        paymentMapper.insert(payment);
        paymentMapper.updateNo(payment.getId(), CodeUtil.gen("SK", payment.getId()));
        refreshStatus(orderId);
        return payment;
    }

    /**
     * 重算订单状态：已签收且收满 → 已完成；有发货 → 已发货；收满 → 已付款；有部分回款 → 部分回款
     */
    public void refreshStatus(Long orderId) {
        CrmOrder order = orderMapper.selectById(orderId);
        if (order == null || "已取消".equals(order.getStatus())) {
            return;
        }
        BigDecimal paid = paymentMapper.sumByOrder(orderId);
        BigDecimal total = order.getAmount();
        int shipments = deliveryMapper.countByOrder(orderId);
        int signed = deliveryMapper.countSigned(orderId);

        String status;
        if (signed > 0 && paid.compareTo(total) >= 0) {
            status = "已完成";
        } else if (shipments > 0) {
            status = "已发货";
        } else if (paid.compareTo(total) >= 0) {
            status = "已付款";
        } else if (paid.compareTo(BigDecimal.ZERO) > 0) {
            status = "部分回款";
        } else {
            status = "待付款";
        }
        if (!status.equals(order.getStatus())) {
            orderMapper.updateStatus(orderId, status);
        }
    }

    /**
     * 校验明细：产品必须存在且在售，同时把规格、成本快照进明细
     */
    private BigDecimal prepareItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new BizException("订单至少要有一行明细");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            if (item.getProductId() == null) {
                throw new BizException("明细里有没选产品的行");
            }
            Product product = productMapper.selectById(item.getProductId());
            if (product == null) {
                throw new BizException("产品不存在");
            }
            if (product.getStatus() != null && product.getStatus() == 0) {
                throw new BizException("产品「" + product.getProductName() + "」已停售，不能下单");
            }
            item.setProductName(product.getProductName());
            item.setSpec(product.getSpec());
            item.setUnit(product.getUnit());
            item.setCost(product.getCost());
            item.setAmount(item.getPrice().multiply(BigDecimal.valueOf(item.getQty())));
            total = total.add(item.getAmount());
        }
        return total;
    }

    private void saveItems(Long orderId, List<OrderItem> items) {
        orderItemMapper.deleteByOrder(orderId);
        for (OrderItem item : items) {
            item.setOrderId(orderId);
            orderItemMapper.insert(item);
        }
    }

    private BigDecimal costOf(CrmOrder order) {
        return order.getCostTotal() == null ? BigDecimal.ZERO : order.getCostTotal();
    }

    private CrmOrder load(Long id) {
        CrmOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        dataScopeService.check(order.getOwnerId(), order.getDeptId());
        return order;
    }
}
