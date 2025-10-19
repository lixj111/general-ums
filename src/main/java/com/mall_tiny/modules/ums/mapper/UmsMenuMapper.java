package com.mall_tiny.modules.ums.mapper;

import com.mall_tiny.modules.ums.model.UmsMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 后台菜单表 Mapper 接口
 * </p>
 *
 * @author baosight
 * @since 2024-09-13
 */
public interface UmsMenuMapper extends BaseMapper<UmsMenu> {
    /**
     * 根据后台用户ID获取目录
     */
    List<UmsMenu> getMenuList(@Param("adminId") Long adminId);

    /**
     * 根据角色ID获取目录
     */
    List<UmsMenu> getMenuListByRoleId(@Param("roleId") Long roleId);

}
