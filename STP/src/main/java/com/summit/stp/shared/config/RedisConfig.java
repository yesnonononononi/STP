package com.summit.stp.shared.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 1. 创建自定义的 ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 设置属性可见性，支持访问任意权限修饰符的属性
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        
        // 注册 JavaTimeModule 支持 Java8 时间格式的 JSON 序列化反序列化
        objectMapper.registerModule(new JavaTimeModule());
        
        // 禁用将日期直接输出为时间戳格式
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 提示：没有在此处调用 activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ...)。
        // 这样可以完全避免把 Java 类信息 "@class": "..." 序列化进 JSON 中，保持在 Redis 缓存里的纯净和可读。

        // 2. 使用 Spring 官方推荐的 RedisSerializer.json() 静态工厂方法，完全避免 deprecation 警告
        // 其底层采用不带 @class 多态头信息的纯 JSON 格式，简洁高效
        RedisSerializer<Object> jacksonSerializer = RedisSerializer.json();

        // 3. String 序列化器
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // 4. 将 Key 的序列化器绑定为 String（使 Redis-Cli 展示正常的 Key 字符串，避免 binary 二进制乱码）
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // 5. 将 Value 序列化器绑定为不带类型信息的 JacksonSerializer 
        template.setValueSerializer(jacksonSerializer);
        template.setHashValueSerializer(jacksonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
