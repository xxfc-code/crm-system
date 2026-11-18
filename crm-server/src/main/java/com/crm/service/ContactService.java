package com.crm.service;

import com.crm.common.BizException;
import com.crm.common.PageResult;
import com.crm.entity.Contact;
import com.crm.mapper.ContactMapper;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService {

    @Resource
    private ContactMapper contactMapper;

    public PageResult<Contact> page(int pageNum, int pageSize, String keyword, Long customerId) {
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(contactMapper.selectList(keyword, customerId));
    }

    public void add(Contact contact) {
        if (contact.getCustomerId() == null) {
            throw new BizException("请选择所属客户");
        }
        contactMapper.insert(contact);
    }

    public void update(Contact contact) {
        if (contactMapper.selectById(contact.getId()) == null) {
            throw new BizException("联系人不存在");
        }
        contactMapper.update(contact);
    }

    public void delete(Long id, boolean force) {
        if (contactMapper.selectById(id) == null) {
            throw new BizException("联系人不存在");
        }
        if (force) {
            contactMapper.deleteForce(id);
        } else {
            contactMapper.deleteLogic(id);
        }
    }
}
