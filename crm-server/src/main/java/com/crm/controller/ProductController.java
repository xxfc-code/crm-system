package com.crm.controller;

import com.crm.common.PageResult;
import com.crm.common.RequiresPerm;
import com.crm.common.Result;
import com.crm.entity.Product;
import com.crm.service.ProductService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Resource
    private ProductService productService;

    @GetMapping("/list")
    public Result<PageResult<Product>> list(@RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Long categoryId,
                                            @RequestParam(required = false) Integer status) {
        return Result.ok(productService.page(pageNum, pageSize, keyword, categoryId, status));
    }

    @GetMapping("/options")
    public Result<List<Product>> options() {
        return Result.ok(productService.options());
    }

    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.ok(productService.get(id));
    }

    @RequiresPerm("crm:product:add")
    @PostMapping
    public Result<Void> add(@RequestBody Product product) {
        productService.add(product);
        return Result.ok();
    }

    @RequiresPerm("crm:product:edit")
    @PutMapping
    public Result<Void> update(@RequestBody Product product) {
        productService.update(product);
        return Result.ok();
    }

    @RequiresPerm("crm:product:remove")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        productService.delete(id, false);
        return Result.ok();
    }

    @RequiresPerm("crm:product:remove")
    @DeleteMapping("/{id}/force")
    public Result<Void> forceRemove(@PathVariable Long id) {
        productService.delete(id, true);
        return Result.ok();
    }
}
