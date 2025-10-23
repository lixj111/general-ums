package com.mall_tiny.security.component;

import cn.hutool.core.util.URLUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 动态权限数据源，用于获取动态权限规则
 */
public class DynamicSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {

    private static Map<String, ConfigAttribute> configAttributeMap = null;

    @Autowired
    private DynamicSecurityService dynamicSecurityService;

    @PostConstruct // 标识一个方法应该在依赖注入完成后立即被调用
    private void loadDataSource() {
        // 获取数据库中的所有 resource 作为configAttributeMap
        configAttributeMap = dynamicSecurityService.loadDataSource();
    }

    public void clearDataSource() {
        configAttributeMap.clear();
        configAttributeMap = null;
    }

    // 根据传入的url字符串，获取对应的配置属性的集合
    // use in DynamicSecurityFilter.java    // InterceptorStatusToken token = super.beforeInvocation(fi);
    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        if (configAttributeMap == null) this.loadDataSource();
        List<ConfigAttribute> configAttributes = new ArrayList<>();
        // 获取当前访问的路径。先将object转为 FilterInvocation 类，然后获取完整的url，从http到最后
        String url = ((FilterInvocation) object).getRequestUrl();
        // url 中端口号之后、查询参数之前的路径
        String path = URLUtil.getPath(url);
        PathMatcher pathMatcher = new AntPathMatcher();
        Iterator<String> iterator = configAttributeMap.keySet().iterator();
        // 获取访问该路径所需资源
        while (iterator.hasNext()) {
            String pattern = iterator.next();
            // 路径匹配，成功则在列表中添加该路径（实际列表中只有一个元素？？？）
            if (pathMatcher.match(pattern, path)) {
                configAttributes.add(configAttributeMap.get(pattern));
            }
        }
        // 未设置操作请求权限，返回空集合
        return configAttributes;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }
}
