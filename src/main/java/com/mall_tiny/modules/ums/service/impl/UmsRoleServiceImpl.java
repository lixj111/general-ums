package com.mall_tiny.modules.ums.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall_tiny.modules.ums.mapper.UmsMenuMapper;
import com.mall_tiny.modules.ums.mapper.UmsResourceMapper;
import com.mall_tiny.modules.ums.model.*;
import com.mall_tiny.modules.ums.mapper.UmsRoleMapper;
import com.mall_tiny.modules.ums.service.UmsAdminCacheService;
import com.mall_tiny.modules.ums.service.UmsRoleMenuRelationService;
import com.mall_tiny.modules.ums.service.UmsRoleResourceRelationService;
import com.mall_tiny.modules.ums.service.UmsRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 后台用户角色表 服务实现类
 * </p>
 *
 * @author baosight
 * @since 2024-09-13
 */
@Service
public class UmsRoleServiceImpl extends ServiceImpl<UmsRoleMapper, UmsRole> implements UmsRoleService {

    @Autowired
    private UmsAdminCacheService adminCacheService;

    @Autowired
    private UmsRoleMenuRelationService roleMenuRelationService;

    @Autowired
    private UmsRoleResourceRelationService roleResourceRelationService;

    @Autowired
    private UmsMenuMapper menuMapper;

    @Autowired
    private UmsResourceMapper resourceMapper;

    @Override
    public boolean create(UmsRole role) {
        role.setCreateTime(new Date());
        role.setAdminCount(0);
        role.setSort(0);
        return save(role);
    }

    @Override
    public boolean delete(List<Long> ids) {
        boolean success = removeByIds(ids);
        adminCacheService.delResourceListByRoles(ids);
        return success;
    }

    @Override
    public Page<UmsRole> list(String keyword, Integer pageSize, Integer pageNum) {
        Page<UmsRole> page = new Page<>(pageNum, pageSize);
        QueryWrapper<UmsRole> wrapper = new QueryWrapper<>();
        LambdaQueryWrapper<UmsRole> lambda = wrapper.lambda();
        if(StrUtil.isNotEmpty(keyword)) {
            lambda.like(UmsRole::getName, keyword);
        }
        return page(page, wrapper);
    }

    @Override
    public List<UmsMenu> getMenuList(Long adminId) {
        return menuMapper.getMenuList(adminId);
    }

    @Override
    public List<UmsMenu> listMenu(Long roleId) {
        return menuMapper.getMenuListByRoleId(roleId);
    }

    @Override
    public List<UmsResource> listResource(Long roleId) {
        return resourceMapper.getResourceListByRoleId(roleId);
    }

    @Override
    public int allocMenu(Long roleId, List<Long> menuIds) {
        // 先删除原有关系
        QueryWrapper<UmsRoleMenuRelation> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UmsRoleMenuRelation::getRoleId, roleId);
        roleMenuRelationService.remove(wrapper);

        // 批量插入新关系
        List<UmsRoleMenuRelation> relationList = new ArrayList<>();
        for (Long menuId : menuIds) {
            UmsRoleMenuRelation relation = new UmsRoleMenuRelation();
            relation.setRoleId(roleId);
            relation.setMenuId(menuId);
            relationList.add(relation);
        }
        roleMenuRelationService.saveBatch(relationList);
        return relationList.size();
    }

    @Override
    public int allocResource(Long roleId, List<Long> resourceIds) {
        // 先删除原有关系
        QueryWrapper<UmsRoleResourceRelation> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UmsRoleResourceRelation::getRoleId, roleId);
        roleResourceRelationService.remove(wrapper);

        // 批量插入新关系
        List<UmsRoleResourceRelation> relationList = new ArrayList<>();
        for (Long resourceId: resourceIds) {
            UmsRoleResourceRelation relation = new UmsRoleResourceRelation();
            relation.setRoleId(roleId);
            relation.setResourceId(resourceId);
            relationList.add(relation);
        }
        roleResourceRelationService.saveBatch(relationList);
        adminCacheService.delResourceListByRole(roleId);
        return relationList.size();
    }
}
