package com.crm.service;

import com.crm.common.BizException;
import com.crm.entity.Dept;
import com.crm.mapper.DeptMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DeptService {

    @Resource
    private DeptMapper deptMapper;

    /**
     * 扁平列表 -> 树。父部门不存在的节点（parentId=0 或父级已被删）当作根节点
     */
    public List<Dept> tree() {
        List<Dept> all = deptMapper.selectAll();
        Map<Long, Dept> map = all.stream().collect(Collectors.toMap(Dept::getId, d -> d));

        List<Dept> roots = new ArrayList<>();
        for (Dept dept : all) {
            Dept parent = map.get(dept.getParentId());
            if (parent == null) {
                roots.add(dept);
            } else {
                parent.getChildren().add(dept);
            }
        }
        return roots;
    }

    public Dept get(Long id) {
        return deptMapper.selectById(id);
    }

    public void add(Dept dept) {
        deptMapper.insert(dept);
    }

    public void update(Dept dept) {
        checkNoCycle(dept.getId(), dept.getParentId());
        deptMapper.update(dept);
    }

    public void delete(Long id, boolean force) {
        if (deptMapper.countChildren(id) > 0) {
            throw new BizException("该部门下还有子部门，不能删除");
        }
        if (deptMapper.countEmployees(id) > 0) {
            throw new BizException("该部门下还有员工，不能删除");
        }
        if (force) {
            deptMapper.deleteForce(id);
        } else {
            deptMapper.deleteLogic(id);
        }
    }

    /**
     * 沿新父级的祖先链向上走，一旦撞到自己就说明成环
     */
    private void checkNoCycle(Long id, Long parentId) {
        Long cursor = parentId;
        while (cursor != null && cursor > 0) {
            if (cursor.equals(id)) {
                throw new BizException("上级部门不能是自己或自己的下级部门");
            }
            Dept parent = deptMapper.selectById(cursor);
            if (parent == null) {
                break;
            }
            cursor = parent.getParentId();
        }
    }
}
