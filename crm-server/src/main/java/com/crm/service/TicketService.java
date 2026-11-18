package com.crm.service;

import com.crm.common.BizException;
import com.crm.common.CodeUtil;
import com.crm.common.DataScope;
import com.crm.common.PageResult;
import com.crm.entity.Employee;
import com.crm.entity.Ticket;
import com.crm.mapper.TicketMapper;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class TicketService {

    @Resource
    private TicketMapper ticketMapper;
    @Resource
    private DataScopeService dataScopeService;

    public PageResult<Ticket> page(int pageNum, int pageSize, String keyword, String status) {
        DataScope ds = dataScopeService.current();
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(ticketMapper.selectList(keyword, status, ds));
    }

    public Ticket get(Long id) {
        return load(id);
    }

    public void add(Ticket ticket) {
        if (ticket.getOwnerId() == null) {
            Employee me = dataScopeService.me();
            ticket.setOwnerId(me.getId());
            ticket.setDeptId(me.getDeptId());
        }
        ticketMapper.insert(ticket);
        ticketMapper.updateNo(ticket.getId(), CodeUtil.gen("GD", ticket.getId()));
    }

    public void update(Ticket ticket) {
        load(ticket.getId());
        ticketMapper.update(ticket);
    }

    public void updateStatus(Long id, String status) {
        load(id);
        ticketMapper.updateStatus(id, status);
    }

    public void delete(Long id, boolean force) {
        load(id);
        if (force) {
            ticketMapper.deleteForce(id);
        } else {
            ticketMapper.deleteLogic(id);
        }
    }

    private Ticket load(Long id) {
        Ticket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new BizException("工单不存在");
        }
        dataScopeService.check(ticket.getOwnerId(), ticket.getDeptId());
        return ticket;
    }
}
