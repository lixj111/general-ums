package com.mall_tiny.modules.ums.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mall_tiny.modules.ums.model.UmsResourceCategory;
import com.mall_tiny.modules.ums.mapper.UmsResourceCategoryMapper;
import com.mall_tiny.modules.ums.service.UmsResourceCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 资源分类表 服务实现类
 * </p>
 *
 * @author baosight
 * @since 2024-09-13
 */
@Service
public class UmsResourceCategoryServiceImpl extends ServiceImpl<UmsResourceCategoryMapper, UmsResourceCategory> implements UmsResourceCategoryService {

    @Override
    public List<UmsResourceCategory> listAll() {
        QueryWrapper<UmsResourceCategory> wrapper = new QueryWrapper<>();
        wrapper.lambda().orderByDesc(UmsResourceCategory::getSort);
        return list(wrapper);
    }

    @Override
    public boolean create(UmsResourceCategory umsResourceCategory) {
        umsResourceCategory.setCreateTime(new Date());
        return save(umsResourceCategory);
    }
}
