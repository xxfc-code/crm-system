package com.crm.service;

import com.crm.common.BizException;
import com.crm.common.CodeUtil;
import com.crm.common.PageResult;
import com.crm.entity.Product;
import com.crm.mapper.ProductMapper;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Resource
    private ProductMapper productMapper;

    public PageResult<Product> page(int pageNum, int pageSize, String keyword, Long categoryId, Integer status) {
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(productMapper.selectList(keyword, categoryId, status));
    }

    public List<Product> options() {
        return productMapper.selectOptions();
    }

    public Product get(Long id) {
        return productMapper.selectById(id);
    }

    public void add(Product product) {
        productMapper.insert(product);
        productMapper.updateNo(product.getId(), CodeUtil.gen("P", product.getId()));
    }

    public void update(Product product) {
        if (productMapper.selectById(product.getId()) == null) {
            throw new BizException("产品不存在");
        }
        productMapper.update(product);
    }

    public void delete(Long id, boolean force) {
        if (productMapper.selectById(id) == null) {
            throw new BizException("产品不存在");
        }
        if (force) {
            productMapper.deleteForce(id);
        } else {
            productMapper.deleteLogic(id);
        }
    }
}
