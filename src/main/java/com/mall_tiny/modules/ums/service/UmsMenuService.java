package com.mall_tiny.modules.ums.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall_tiny.modules.ums.dto.UmsMenuNode;
import com.mall_tiny.modules.ums.model.UmsMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 后台菜单表 服务类
 * </p>
 *
 * @author baosight
 * @since 2024-09-13
 */
public interface UmsMenuService extends IService<UmsMenu> {
    /**
     * 创建后台目录
     */
    boolean create(UmsMenu umsMenu);

    /**
     * 修改后台目录
     */
    boolean update(Long id, UmsMenu umsMenu);

    /**
     * 分页查询后台目录
     */
    Page<UmsMenu> list(Long parentId, Integer pageSize, Integer pageNum);

    /**
     * 树形结构返回所有目录列表
     */
    List<UmsMenuNode> treeList();

    /**
     * 修改目录列表状态
     */
    boolean updateHidden(Long id, Integer hidden);
}
