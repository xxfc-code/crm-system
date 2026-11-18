package com.crm.controller;

import com.crm.common.PageResult;
import com.crm.common.RequiresPerm;
import com.crm.common.Result;
import com.crm.entity.Ticket;
import com.crm.service.TicketService;
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
@RequestMapping("/api/ticket")
public class TicketController {

    @Resource
    private TicketService ticketService;

    @GetMapping("/list")
    public Result<PageResult<Ticket>> list(@RequestParam(defaultValue = "1") int pageNum,
                                           @RequestParam(defaultValue = "10") int pageSize,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String status) {
        return Result.ok(ticketService.page(pageNum, pageSize, keyword, status));
    }

    @GetMapping("/{id}")
    public Result<Ticket> detail(@PathVariable Long id) {
        return Result.ok(ticketService.get(id));
    }

    @RequiresPerm("crm:ticket:add")
    @PostMapping
    public Result<Void> add(@RequestBody Ticket ticket) {
        ticketService.add(ticket);
        return Result.ok();
    }

    @RequiresPerm("crm:ticket:edit")
    @PutMapping
    public Result<Void> update(@RequestBody Ticket ticket) {
        ticketService.update(ticket);
        return Result.ok();
    }

    @RequiresPerm("crm:ticket:edit")
    @PutMapping("/{id}/status")
    public Result<Void> status(@PathVariable Long id, @RequestParam String status) {
        ticketService.updateStatus(id, status);
        return Result.ok();
    }

    @RequiresPerm("crm:ticket:remove")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        ticketService.delete(id, false);
        return Result.ok();
    }

    @RequiresPerm("crm:ticket:remove")
    @DeleteMapping("/{id}/force")
    public Result<Void> forceRemove(@PathVariable Long id) {
        ticketService.delete(id, true);
        return Result.ok();
    }
}
