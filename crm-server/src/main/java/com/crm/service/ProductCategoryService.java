package com.crm.service;

import com.crm.common.BizException;
import com.crm.entity.ProductCategory;
import com.crm.mapper.ProductCategoryMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductCategoryService {

    @Resource
    private ProductCategoryMapper categoryMapper;

    public List<ProductCategory> list() {
        return categoryMapper.selectList();
    }

    public void add(ProductCategory category) {
        categoryMapper.insert(category);
    }

    public void update(ProductCategory category) {
        categoryMapper.update(category);
    }

    public void delete(Long id, boolean force) {
        if (categoryMapper.selectById(id) == null) {
            throw new BizException("分类不存在");
        }
        if (categoryMapper.countProducts(id) > 0) {
            throw new BizException("该分类下还有产品，不能删除");
        }
        if (force) {
            categoryMapper.deleteForce(id);
        } else {
            categoryMapper.deleteLogic(id);
        }
    }
}
