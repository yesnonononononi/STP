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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class SystemMessageConverter {
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

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
        
        // Timestamp -> ISO-8601 String
        simpleModule.addSerializer(Timestamp.class, new StdSerializer<Timestamp>(Timestamp.class) {

            @Override
            public void serialize(Timestamp value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
                if(ctxt == null){
                    gen.writeNull();
                }
                else {
                    ZonedDateTime zonedDateTime = value.toInstant().atZone(ZoneId.systemDefault());
                    gen.writeString(zonedDateTime.format(ISO_FORMATTER));
                }
            }
        });

        return JsonMapper.builder()
                .addModule(simpleModule)
                .build();
    }
}
