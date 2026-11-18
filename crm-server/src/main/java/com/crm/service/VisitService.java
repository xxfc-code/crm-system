package com.crm.service;

import com.crm.common.BizException;
import com.crm.common.DataScope;
import com.crm.common.PageResult;
import com.crm.common.UserContext;
import com.crm.entity.Visit;
import com.crm.mapper.VisitMapper;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class VisitService {

    @Resource
    private VisitMapper visitMapper;
    @Resource
    private DataScopeService dataScopeService;

    public PageResult<Visit> page(int pageNum, int pageSize, String keyword, String way) {
        DataScope ds = dataScopeService.current();
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(visitMapper.selectList(keyword, way, ds));
    }

    public void add(Visit visit) {
        if (visit.getVisitorId() == null) {
            visit.setVisitorId(UserContext.get());
        }
        visitMapper.insert(visit);
    }

    public void update(Visit visit) {
        load(visit.getId());
        visitMapper.update(visit);
    }

    public void delete(Long id, boolean force) {
        load(id);
        if (force) {
            visitMapper.deleteForce(id);
        } else {
            visitMapper.deleteLogic(id);
        }
    }

    /** 回访按记录人校验，没记录人的历史数据放行 */
    private Visit load(Long id) {
        Visit visit = visitMapper.selectById(id);
        if (visit == null) {
            throw new BizException("回访记录不存在");
        }
        if (visit.getVisitorId() != null) {
            dataScopeService.check(visit.getVisitorId(), null);
        }
        return visit;
    }
}
