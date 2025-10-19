package com.mall_tiny.modules.ums.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall_tiny.modules.ums.dto.UmsMenuNode;
import com.mall_tiny.modules.ums.model.UmsMenu;
import com.mall_tiny.modules.ums.mapper.UmsMenuMapper;
import com.mall_tiny.modules.ums.service.UmsMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 后台菜单表 服务实现类
 * </p>
 *
 * @author baosight
 * @since 2024-09-13
 */
@Service
public class UmsMenuServiceImpl extends ServiceImpl<UmsMenuMapper, UmsMenu> implements UmsMenuService {

    @Override
    public boolean create(UmsMenu umsMenu) {
        umsMenu.setCreateTime(new Date());
        updateLevel(umsMenu);
        return save(umsMenu);
    }

    /**
     * 为目录设置目录层级
     */
    private void updateLevel(UmsMenu umsMenu) {
        if (umsMenu.getParentId() == 0) {
            // 没有父级目录时则为一级目录
            umsMenu.setLevel(0);
        } else {
            // 有父级目录时，根据父级目录的level设置
            UmsMenu parentMenu = getById(umsMenu.getParentId());
            if (parentMenu != null) {
                umsMenu.setLevel(parentMenu.getLevel() + 1);
            } else {
                umsMenu.setLevel(0);
            }
        }
    }

    @Override
    public boolean update(Long id, UmsMenu umsMenu) {
        umsMenu.setId(id);
        updateLevel(umsMenu);
        return updateById(umsMenu);
    }

    @Override
    public Page<UmsMenu> list(Long parentId, Integer pageSize, Integer pageNum) {
        Page<UmsMenu> page = new Page<>(pageNum, pageSize);
        QueryWrapper<UmsMenu> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UmsMenu::getParentId, parentId)
                .orderByDesc(UmsMenu::getSort);
        return page(page, wrapper);
    }

    @Override
    public List<UmsMenuNode> treeList() {
        List<UmsMenu> menuList = list();
        List<UmsMenuNode> result = menuList.stream()
                .filter(menu -> menu.getParentId().equals(0L))
//                过滤满足条件的menu，否则将被拦截
//                即保留属性parentId等于0的menu，其余丢弃
                .map(menu -> convertMenuNode(menu, menuList)).collect(Collectors.toList());
        return result;
    }

    @Override
    public boolean updateHidden(Long id, Integer hidden) {
        UmsMenu umsMenu = new UmsMenu();
        umsMenu.setId(id);
        umsMenu.setHidden(hidden);
        return updateById(umsMenu);
    }

    /**
     * 将UmsMenu装化为UmsMenuNode并设置children的属性
     */
    private UmsMenuNode convertMenuNode(UmsMenu menu, List<UmsMenu> menuList) {
        UmsMenuNode node = new UmsMenuNode();
        BeanUtils.copyProperties(menu, node);
        List<UmsMenuNode> children = menuList.stream()
                .filter(subMenu -> subMenu.getParentId().equals(menu.getId()))
                .map(subMenu -> convertMenuNode(subMenu, menuList)).collect(Collectors.toList());
        node.setChildren(children);
        return node;
    }
}
