package com.crm.service;

import com.crm.common.BizException;
import com.crm.common.CodeUtil;
import com.crm.common.DataScope;
import com.crm.common.PageResult;
import com.crm.common.UserContext;
import com.crm.entity.Employee;
import com.crm.entity.OrderItem;
import com.crm.entity.CrmOrder;
import com.crm.entity.Quotation;
import com.crm.entity.QuotationItem;
import com.crm.entity.Product;
import com.crm.mapper.OrderMapper;
import com.crm.mapper.OpportunityMapper;
import com.crm.mapper.ProductMapper;
import com.crm.mapper.QuotationMapper;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class QuotationService {

    @Resource
    private QuotationMapper quotationMapper;
    @Resource
    private DataScopeService dataScopeService;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OpportunityMapper opportunityMapper;
    @Resource
    private OrderService orderService;
    @Resource
    private ProductMapper productMapper;

    /** 超过这个金额的报价需要审批 */
    @Value("${crm.quotation.approve-threshold:50000}")
    private BigDecimal approveThreshold;

    public PageResult<Quotation> page(int pageNum, int pageSize, String keyword, String status) {
        DataScope ds = dataScopeService.current();
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(quotationMapper.selectList(keyword, status, ds));
    }

    /** 详情才带明细，列表不查 */
    public Quotation get(Long id) {
        Quotation quotation = load(id);
        quotation.setItems(quotationMapper.selectItems(id));
        return quotation;
    }

    @Transactional
    public void add(Quotation quotation) {
        if (quotation.getOwnerId() == null) {
            Employee me = dataScopeService.me();
            quotation.setOwnerId(me.getId());
            quotation.setDeptId(me.getDeptId());
        }
        quotation.setTotalAmount(sum(quotation));
        applyApproveRule(quotation);
        quotationMapper.insert(quotation);
        quotationMapper.updateNo(quotation.getId(), CodeUtil.gen("BJ", quotation.getId()));
        saveItems(quotation);
        syncOpportunity(quotation);
    }

    @Transactional
    public void update(Quotation quotation) {
        load(quotation.getId());
        quotation.setTotalAmount(sum(quotation));
        applyApproveRule(quotation);
        quotationMapper.update(quotation);
        // 明细整体重建，比算增删改简单
        quotationMapper.deleteItems(quotation.getId());
        saveItems(quotation);
        syncOpportunity(quotation);
    }

    public void updateStatus(Long id, String status) {
        load(id);
        quotationMapper.updateStatus(id, status);
    }

    /**
     * 报价单确认后转成订单：明细、金额、客户全部带过去，避免重复录入
     */
    @Transactional
    public Long convertToOrder(Long quotationId) {
        Quotation quotation = quotationMapper.selectById(quotationId);
        if (quotation == null) {
            throw new BizException("报价单不存在");
        }
        if (!"已确认".equals(quotation.getStatus())) {
            throw new BizException("只有已确认的报价单才能转成订单");
        }
        if (quotation.getApproveStatus() != null && quotation.getApproveStatus() == 1) {
            throw new BizException("这张报价单还在待审批，审批通过后才能转订单");
        }
        if (orderMapper.countByQuote(quotationId) > 0) {
            throw new BizException("该报价单已经生成过订单了");
        }
        List<QuotationItem> items = quotationMapper.selectItems(quotationId);
        if (items.isEmpty()) {
            throw new BizException("报价单没有明细，转不了订单");
        }

        CrmOrder order = new CrmOrder();
        order.setCustomerId(quotation.getCustomerId());
        order.setOppId(quotation.getOppId());
        order.setQuoteId(quotationId);
        order.setOwnerId(quotation.getOwnerId());
        order.setDeptId(quotation.getDeptId());
        order.setSignDate(LocalDate.now());
        order.setRemark("由报价单 " + quotation.getQuoteNo() + " 生成");

        List<OrderItem> orderItems = items.stream().map(item -> {
            OrderItem oi = new OrderItem();
            oi.setProductId(item.getProductId());
            oi.setProductName(item.getProductName());
            oi.setQty(item.getQty());
            oi.setPrice(item.getPrice());
            return oi;
        }).toList();
        order.setItems(orderItems);

        orderService.add(order);
        return order.getId();
    }

    @Transactional
    public void delete(Long id, boolean force) {
        load(id);
        if (force) {
            quotationMapper.deleteForce(id);
        } else {
            quotationMapper.deleteLogic(id);
        }
        quotationMapper.deleteItems(id);
    }

    private void saveItems(Quotation quotation) {
        for (QuotationItem item : quotation.getItems()) {
            item.setQuotationId(quotation.getId());
            // 名称和成本都以产品表为准，不信前端传过来的
            Product product = productMapper.selectById(item.getProductId());
            if (product == null) {
                throw new BizException("产品不存在");
            }
            item.setProductName(product.getProductName());
            item.setCost(product.getCost());
            item.setAmount(item.getPrice().multiply(BigDecimal.valueOf(item.getQty())));
            quotationMapper.insertItem(item);
        }
    }

    /**
     * 审批：大额报价需要主管点头才能转订单
     */
    public void approve(Long id, boolean approved, String remark) {
        Quotation quotation = load(id);
        if (quotation.getApproveStatus() == null || quotation.getApproveStatus() != 1) {
            throw new BizException("这张报价单当前不在待审批状态");
        }
        quotationMapper.updateApprove(id, approved ? 2 : 3, UserContext.get(), remark);
    }

    /** 超过阈值就自动转待审批 */
    private void applyApproveRule(Quotation quotation) {
        int status = quotation.getTotalAmount().compareTo(approveThreshold) >= 0 ? 1 : 0;
        quotation.setApproveStatus(status);
    }

    /** 报价金额回写到商机，保持两边口径一致 */
    private void syncOpportunity(Quotation quotation) {
        if (quotation.getOppId() != null) {
            opportunityMapper.updateAmount(quotation.getOppId(), quotation.getTotalAmount());
        }
    }

    private BigDecimal sum(Quotation quotation) {
        return quotation.getItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Quotation load(Long id) {
        Quotation quotation = quotationMapper.selectById(id);
        if (quotation == null) {
            throw new BizException("报价单不存在");
        }
        dataScopeService.check(quotation.getOwnerId(), quotation.getDeptId());
        return quotation;
    }
}
