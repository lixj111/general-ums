package com.mall_tiny.security.config;

import com.mall_tiny.security.component.*;
import com.mall_tiny.security.util.JwtTokenUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

/**
 * SpringSecurity通用配置
 * 包括：通用Bean、Security通用Bean、动态权限通用Bean
 * 仅用于将各个类注册成为 Spring Bean，不涉及具体的业务功能
 */
@Configuration
public class CommonSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtTokenUtil jwtTokenUtil() {
        return new JwtTokenUtil();
    }

    @Bean
    public IgnoreUrlsConfig ignoreUrlsConfig() {
        IgnoreUrlsConfig ignoreUrlsConfig = new IgnoreUrlsConfig();
        List<String> urls = new ArrayList<>();
        urls.add("/admin/register");
        urls.add("/admin/login");
        urls.add("/admin/refreshToken");
        urls.add("/admin/logout");
        urls.add("/swagger-ui/**");
        urls.add("/swagger-resources/**");
        urls.add("/.well-known/**");
        urls.add("/v2/api-docs");
        urls.add("/v2/api-docs/**");
        urls.add("/webjars/**");
        urls.add("/error");
        ignoreUrlsConfig.setUrls(urls);
        return ignoreUrlsConfig ;
    }

    @Bean
    public RestfulAccessDeniedHandler restfulAccessDeniedHandler() {
        return new RestfulAccessDeniedHandler();
    }

    @Bean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    @Bean
    public JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter() {
        return new JwtAuthenticationTokenFilter();
    }

    @Bean
    public DynamicSecurityMetadataSource dynamicSecurityMetadataSource() {
        return new DynamicSecurityMetadataSource();
    }

    @Bean
    public DynamicAccessDecisionManager dynamicAccessDecisionManager() {
        return new DynamicAccessDecisionManager();
    }

    @Bean
    public DynamicSecurityFilter dynamicSecurityFilter() {
        return new DynamicSecurityFilter();
    }
}
