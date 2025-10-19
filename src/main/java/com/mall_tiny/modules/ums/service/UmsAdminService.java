package com.mall_tiny.modules.ums.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall_tiny.modules.ums.dto.UmsAdminParam;
import com.mall_tiny.modules.ums.dto.UpdateAdminPasswordParam;
import com.mall_tiny.modules.ums.model.UmsAdmin;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall_tiny.modules.ums.model.UmsResource;
import com.mall_tiny.modules.ums.model.UmsRole;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 后台用户表 服务类
 * </p>
 *
 * @author baosight
 * @since 2024-10-21
 */
public interface UmsAdminService extends IService<UmsAdmin> {

    /**
     * 根据用户名获取后台管理员
     * @param username 用户名
     * @return 返回后台管理员信息
     */
    UmsAdmin getAdminByUsername(String username);

    /**
     * 注册功能
     */
    UmsAdmin register(UmsAdminParam umsAdminParam);

    /**
     * 登录功能
     * @param username 用户名
     * @param password 密码
     * @return 生成的JWT的token值
     */
    String login(String username, String password);

    /**
     * 刷新token的功能
     * @param oldToken 旧的token
     */
    String refreshToken(String oldToken);

    /**
     * 根据用户名或昵称分页查找用户
     */
    // TODO: 根据用户名或昵称分页查找用户
    Page<UmsAdmin> list(String keyword, Integer pageSize, Integer pageNum);

    /**
     * 修改指定用户的信息
     * @param id 用户id
     * @param admin 用户
     */
    boolean update(Long id, UmsAdmin admin);

    /**
     * 删除指定用户
     */
    boolean delete(Long id);

    /**
     * 修改用户角色关系
     */
    @Transactional
    int updateRole(Long adminId, List<Long> roleIds);

    /**
     * 获取用户的角色
     */
    List<UmsRole> getRoleList(Long adminId);

    /**
     * 获取用户可访问的资源列表
     * @param adminId 用户ID
     * @return 资源列表
     */
    List<UmsResource> getResourceList(Long adminId);

    /**
     * 修改密码
     */
    int updatePassword(UpdateAdminPasswordParam updatePasswordParam);

    /**
     * 获取用户信息
     * @param username 用户名
     * @return 用户信息
     */
    UserDetails loadUserByUsername(String username);

    /**
     * 获取缓存服务
     */
    UmsAdminCacheService getCacheService();
}
