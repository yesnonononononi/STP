package com.summit.stp.shared.config;


import cn.hutool.core.date.DateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.sql.Date;
import java.time.LocalDateTime;

@Configuration
public class SystemMessageConverter {
    @Bean
    @Primary
    public JsonMapper objectMapper() {
        SimpleModule simpleModule = new SimpleModule();
        //Long -> String
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);

        //DataTime -> String
        simpleModule.addSerializer(LocalDateTime.class,ToStringSerializer.instance);
        simpleModule.addSerializer(Date.class,ToStringSerializer.instance);
        simpleModule.addSerializer(DateTime.class,ToStringSerializer.instance);

        return JsonMapper.builder()
                .addModule(simpleModule)
                .build();
    }
}
