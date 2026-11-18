package com.crm.service;

import com.crm.common.BizException;
import com.crm.entity.Menu;
import com.crm.mapper.MenuMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MenuService {

    @Resource
    private MenuMapper menuMapper;

    public List<Menu> allTree() {
        return buildTree(menuMapper.selectAll());
    }

    public List<Menu> manageTree() {
        return buildTree(menuMapper.selectAllForManage());
    }

    public void add(Menu menu) {
        menuMapper.insert(menu);
    }

    public void update(Menu menu) {
        if (menuMapper.selectById(menu.getId()) == null) {
            throw new BizException("菜单不存在");
        }
        menuMapper.update(menu);
    }

    public void delete(Long id) {
        if (menuMapper.countChildren(id) > 0) {
            throw new BizException("还有子菜单，先删下级");
        }
        if (menuMapper.countRoleRef(id) > 0) {
            throw new BizException("还有角色在用这个菜单，先去角色里取消授权");
        }
        menuMapper.deleteById(id);
    }

    public List<Menu> buildTree(List<Menu> list) {
        Map<Long, Menu> map = list.stream().collect(Collectors.toMap(Menu::getId, m -> m));
        List<Menu> roots = new ArrayList<>();
        for (Menu menu : list) {
            Menu parent = map.get(menu.getParentId());
            if (parent == null) {
                roots.add(menu);
            } else {
                parent.getChildren().add(menu);
            }
        }
        return roots;
    }
}
