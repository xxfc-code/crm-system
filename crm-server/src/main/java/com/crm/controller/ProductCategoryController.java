package com.crm.controller;

import com.crm.common.RequiresPerm;
import com.crm.common.Result;
import com.crm.entity.ProductCategory;
import com.crm.service.ProductCategoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/productCategory")
public class ProductCategoryController {

    @Resource
    private ProductCategoryService categoryService;

    @GetMapping("/list")
    public Result<List<ProductCategory>> list() {
        return Result.ok(categoryService.list());
    }

    @RequiresPerm("crm:productCategory:add")
    @PostMapping
    public Result<Void> add(@RequestBody ProductCategory category) {
        categoryService.add(category);
        return Result.ok();
    }

    @RequiresPerm("crm:productCategory:edit")
    @PutMapping
    public Result<Void> update(@RequestBody ProductCategory category) {
        categoryService.update(category);
        return Result.ok();
    }

    @RequiresPerm("crm:productCategory:remove")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        categoryService.delete(id, false);
        return Result.ok();
    }

    @RequiresPerm("crm:productCategory:remove")
    @DeleteMapping("/{id}/force")
    public Result<Void> forceRemove(@PathVariable Long id) {
        categoryService.delete(id, true);
        return Result.ok();
    }
}
