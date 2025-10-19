package com.mall_tiny.security.config;

import com.mall_tiny.security.component.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SpringSecurity 是 spring boot security 5.4.x 以上新用法配置
 * 为避免循环依赖，仅用于配置HttpSecurity
 */
@Configuration
public class SecurityConfig {

    @Autowired
    private IgnoreUrlsConfig ignoreUrlsConfig;

    @Autowired
    public RestfulAccessDeniedHandler restfulAccessDeniedHandler;

    @Autowired
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Autowired
    public JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Autowired
    public DynamicSecurityService dynamicSecurityService;

    @Autowired
    public DynamicSecurityFilter dynamicSecurityFilter;

    // 规则顺序：Spring Security按照定义的顺序来检查请求匹配规则，并且一旦找到一个匹配项就会停止进一步检查。
    // 因此，如果你首先定义了不需要保护的资源路径（使用 permitAll()），那么这些路径将不会受到后续更严格的规则的影响。
    @Bean
    SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry = httpSecurity
                .authorizeRequests();
        // 不需要保护的资源路径通过访问
        for (String url: ignoreUrlsConfig.getUrls()) {
            registry.antMatchers(url).permitAll();
        }
        // 允许跨域请求的OPTIONS请求
        registry.antMatchers(HttpMethod.OPTIONS).permitAll();
        // 任何请求需要身份验证
        registry.and()
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                // 关闭跨站请求防护及不使用session
                .and()
                .csrf()
                .disable()
                .exceptionHandling()
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .accessDeniedHandler(restfulAccessDeniedHandler)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                // 自定义权限拒绝处理类
                .and()
//                .addFilterBefore(jwtAuthenticationTokenFilter, FilterSecurityInterceptor.class);
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        // 有动态权限配置时添加动态权限校验过滤器
        if (dynamicSecurityService != null) {
            //
            registry.and().addFilterBefore(dynamicSecurityFilter, FilterSecurityInterceptor.class);
        }
        return httpSecurity.build();
    }
}
