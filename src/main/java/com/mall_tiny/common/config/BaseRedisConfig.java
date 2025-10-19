package com.mall_tiny.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.mall_tiny.common.service.RedisService;
import com.mall_tiny.common.service.impl.RedisServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

public class BaseRedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisSerializer<Object> serializer = redisSerializer();
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 设置连接工厂，会根据配置文件application.yml、application.properties等文件读取连接信息
        /**
         * spring:
         *   redis:
         *     host: localhost # Redis服务器地址
         *     database: 0 # Redis数据库索引（默认为0）
         *     port: 6379 #Redis连接端口
         *     password: # Redis服务器连接地址（默认为空）
         *     timeout: 3000ms # 连接超时时间（毫秒）
         */
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 设置键值对的序列化器，这里仅设置了普通键值对、Hash结构的序列化器。未设置Set结构、List结构
        // StringRedisSerializer():用于将字符串类型的键和哈希键序列化为字节流
        // serializer：基于Jackson库的自定义Redis序列化器，能够对Java对象进行序列化和反序列化
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(serializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

//    @Bean

    /**
     * 序列化器的作用是将Java对象转化为Redis中的字节流，或将Redis中读取的字节流转为Java对象
     */
    public RedisSerializer<Object> redisSerializer() {
        // 创建json序列化器，使用基于Jackson库的Redis序列化器，能够对Java对象进行序列化和反序列化
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper objectMapper = new ObjectMapper();
        // 设置所有属性的可见性为ANY，无论属性的访问级别是什么（public、private等），都可被序列化和反序列化
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // 必须设置，否则无法将JSON转化为对象，而是转化为map类型
        // 激活默认的类型信息处理器，以便能够正确处理多态类型，NON_FINAL表示只对非final类型进行类型信息处理
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(objectMapper);
        return serializer;
    }

    @Bean
    public RedisService redisService() {
        return new RedisServiceImpl();
    }
}
