package com.crm.controller;

import com.crm.common.PageResult;
import com.crm.common.RequiresPerm;
import com.crm.common.Result;
import com.crm.entity.Contact;
import com.crm.service.ContactService;
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

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    @Resource
    private ContactService contactService;

    @GetMapping("/list")
    public Result<PageResult<Contact>> list(@RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Long customerId) {
        return Result.ok(contactService.page(pageNum, pageSize, keyword, customerId));
    }

    @RequiresPerm("crm:contact:add")
    @PostMapping
    public Result<Void> add(@RequestBody Contact contact) {
        contactService.add(contact);
        return Result.ok();
    }

    @RequiresPerm("crm:contact:edit")
    @PutMapping
    public Result<Void> update(@RequestBody Contact contact) {
        contactService.update(contact);
        return Result.ok();
    }

    @RequiresPerm("crm:contact:remove")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        contactService.delete(id, false);
        return Result.ok();
    }

    @RequiresPerm("crm:contact:remove")
    @DeleteMapping("/{id}/force")
    public Result<Void> forceRemove(@PathVariable Long id) {
        contactService.delete(id, true);
        return Result.ok();
    }
}
