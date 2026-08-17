package com.summit.stp.common.codec.config;

import com.summit.stp.common.codec.IdCodec;
import com.summit.stp.common.codec.SimpleIdCodec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * IdCodec 自动配置类
 */
@Configuration
public class IdCodecConfiguration {

    @Bean
    @ConditionalOnMissingBean(IdCodec.class)
    public IdCodec idCodec() {
        return new SimpleIdCodec();
    }
}
