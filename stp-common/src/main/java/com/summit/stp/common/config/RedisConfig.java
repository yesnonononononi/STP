package com.summit.stp.common.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.lettuce.core.ClientOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class RedisConfig {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        // 反射修改 Lettuce 离线瞬间熔断行为，避免引入 Spring autoconfigure 带来的版本冲突
        try {
            if (connectionFactory instanceof LettuceConnectionFactory lettuceFactory) {
                java.lang.reflect.Field clientField = LettuceConnectionFactory.class.getDeclaredField("client");
                clientField.setAccessible(true);
                Object client = clientField.get(lettuceFactory);
                if (client instanceof io.lettuce.core.AbstractRedisClient redisClient) {
                    ClientOptions clientOptions = ClientOptions.builder()
                            .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                            .build();
                    java.lang.reflect.Method setOptionsMethod = io.lettuce.core.AbstractRedisClient.class.getDeclaredMethod("setOptions", ClientOptions.class);
                    setOptionsMethod.setAccessible(true);
                    setOptionsMethod.invoke(redisClient, clientOptions);
                }
            }
        } catch (Exception e) {
            System.err.println("【RedisConfig】反射设置 Lettuce 熔断策略失败: " + e.getMessage());
        }

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = createRedisObjectMapper();

        // 将 Value 序列化器绑定为带类型信息的 JacksonSerializer
        GenericJacksonJsonRedisSerializer serializer = new GenericJacksonJsonRedisSerializer(objectMapper);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setValueSerializer(serializer);
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    private ObjectMapper createRedisObjectMapper() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(ISO_FORMATTER));
        simpleModule.addSerializer(LocalDate.class, new LocalDateSerializer(DATE_FMT));
        simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(ISO_FORMATTER));
        simpleModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DATE_FMT));

        BasicPolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Object.class)
                .build();

        return JsonMapper.builder()
                .addModule(simpleModule)
                .activateDefaultTyping(
                        ptv,
                        DefaultTyping.NON_FINAL,
                        JsonTypeInfo.As.PROPERTY
                )
                .build();
    }

}
