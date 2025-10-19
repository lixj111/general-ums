package com.mall_tiny.domain;

import com.mall_tiny.modules.ums.model.UmsAdmin;
import com.mall_tiny.modules.ums.model.UmsResource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AdminUserDetails implements UserDetails {
    private UmsAdmin umsAdmin;
    private List<UmsResource> resourceList;

    public AdminUserDetails(UmsAdmin umsAdmin, List<UmsResource> resourceList) {
        this.umsAdmin = umsAdmin;
        this.resourceList = resourceList;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 返回当前用户的角色
        return resourceList.stream()
                .map(resource -> new SimpleGrantedAuthority(resource.getId() + ":" + resource.getName()))
                .collect(Collectors.toList());
        /**
         * 执行流程：
         *   1. resourceList.stream()  对集合创建一个流
         *   2. .map(resource -> new SimpleGrantedAuthority(resource.getId() + ":" + resource.getName()))
         *      将流中的每一个元素映射为一个新的SimpleGrantedAuthority类，接受一个字符串参数（”{id}：{Name}“）
         *   3. 。collect(Collectors.toList())  将流中的元素收集形成一个新的List<SimpleGrantedAuthority>, 这意味着结果是一个SimpleGrantedAuthority的列表
         *
         * 资源列表：[(id=1, createTime=Tue Feb 04 17:04:55 CST 2020, name=商品品牌管理, url=/brand/**, description=null, categoryId=1)...]
         * 输出结果：[1:商品品牌管理, 2:商品属性分类管理, 3:商品属性管理]
         */
    }

    @Override
    public String getPassword() {
        return umsAdmin.getPassword();
    }

    @Override
    public String getUsername() {
        return umsAdmin.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return umsAdmin.getStatus().equals(1);
    }
}
