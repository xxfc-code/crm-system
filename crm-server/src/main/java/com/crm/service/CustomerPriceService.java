package com.crm.service;

import com.crm.common.BizException;
import com.crm.common.PageResult;
import com.crm.entity.CustomerPrice;
import com.crm.mapper.CustomerPriceMapper;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerPriceService {

    @Resource
    private CustomerPriceMapper priceMapper;

    public PageResult<CustomerPrice> page(int pageNum, int pageSize, Long customerId) {
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(priceMapper.selectList(customerId));
    }

    public void add(CustomerPrice price) {
        if (price.getCustomerId() == null || price.getProductId() == null) {
            throw new BizException("客户和产品都要选");
        }
        priceMapper.insert(price);
    }

    public void update(CustomerPrice price) {
        if (priceMapper.selectById(price.getId()) == null) {
            throw new BizException("价格记录不存在");
        }
        priceMapper.update(price);
    }

    public void delete(Long id, boolean force) {
        if (priceMapper.selectById(id) == null) {
            throw new BizException("价格记录不存在");
        }
        if (force) {
            priceMapper.deleteForce(id);
        } else {
            priceMapper.deleteLogic(id);
        }
    }
}
