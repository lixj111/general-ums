package com.mall_tiny.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 全局跨域设置
 */
@Configuration
public class GlobalCorsConfig {

    /**
     * 允许跨域调用的过滤器
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有域名进行跨域调用
        config.addAllowedOriginPattern("*");
        //该用法在SpringBoot 2.7.0中已不再支持
        //config.addAllowedOrigin("*");

        // 允许跨域发送cookie
        config.setAllowCredentials(true);

        // 放行全部原始头信息，允许所有HTTP请求方法进行跨域调用（GET、POST、UPDATE、DELETE等）
        config.addAllowedHeader("*");

        //允许所有请求方法跨域调用
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 将cors配置注册到所有的url路径上
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
