package com.mall_tiny.modules.ums.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall_tiny.modules.ums.model.UmsResource;
import com.mall_tiny.modules.ums.mapper.UmsResourceMapper;
import com.mall_tiny.modules.ums.service.UmsAdminCacheService;
import com.mall_tiny.modules.ums.service.UmsResourceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * <p>
 * 后台资源表 服务实现类
 * </p>
 *
 * @author baosight
 * @since 2024-09-13
 */
@Service
public class UmsResourceServiceImpl extends ServiceImpl<UmsResourceMapper, UmsResource> implements UmsResourceService {

    @Autowired
    private UmsAdminCacheService adminCacheService;

    @Override
    public boolean create(UmsResource umsResource) {
        umsResource.setCreateTime(new Date());
        return save(umsResource);
    }

    @Override
    public boolean update(Long id, UmsResource umsResource) {
        umsResource.setId(id);
        boolean success = updateById(umsResource);
        adminCacheService.delResourceListByResource(id);
        return success;
    }

    @Override
    public boolean delete(Long id) {
        boolean success = removeById(id);
        adminCacheService.delResourceListByResource(id);
        return false;
    }

    @Override
    public Page<UmsResource> list(Long categoryId, String nameKeyWord, String urlKeyWord, Integer pageSize, Integer pageNum) {
        Page<UmsResource> page = new Page<>(pageNum, pageSize);
        QueryWrapper<UmsResource> wrapper = new QueryWrapper<>();
        LambdaQueryWrapper<UmsResource> lambda = wrapper.lambda();
        if (categoryId != null) {
            lambda.eq(UmsResource::getCategoryId, categoryId);
        }
        if (StrUtil.isNotEmpty(nameKeyWord)) {
            lambda.like(UmsResource::getName, nameKeyWord);
        }
        if (StrUtil.isNotEmpty(urlKeyWord)) {
            lambda.like(UmsResource::getUrl, urlKeyWord);
        }
        return page(page, wrapper);
    }
}
