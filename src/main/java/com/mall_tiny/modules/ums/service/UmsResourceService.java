package com.mall_tiny.modules.ums.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall_tiny.modules.ums.model.UmsResource;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 后台资源表 服务类
 * </p>
 *
 * @author baosight
 * @since 2024-09-13
 */
public interface UmsResourceService extends IService<UmsResource> {

    /**
     * 添加资源
     */
    boolean create(UmsResource umsResource);

    /**
     * 修改资源
     */
    boolean update(Long id, UmsResource umsResource);

    /**
     * 删除资源
     */
    boolean delete(Long id);

    /**
     * 分页查询资源
     */
    Page<UmsResource> list(Long categoryId, String nameKeyWord, String urlKeyWord, Integer pageSize, Integer pageNum);
}
