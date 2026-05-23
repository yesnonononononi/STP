package com.summit.stp.shared.config;


import cn.hutool.core.date.DateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class SystemMessageConverter {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
        
        // Timestamp -> Long (时间戳毫秒数)
        simpleModule.addSerializer(Timestamp.class, new StdSerializer<Timestamp>(Timestamp.class) {

            @Override
            public void serialize(Timestamp value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
                if(ctxt == null){
                    gen.writeNull();
                }
                else {
                    gen.writeNumber(value.getTime());
                }
            }
        });

        return JsonMapper.builder()
                .addModule(simpleModule)
                .build();
    }
}
