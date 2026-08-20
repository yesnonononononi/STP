package com.summit.stp.message.infrastructure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Data
@Component
@ConfigurationProperties(prefix = "ws.session")
public class ImProperties {
    private Integer pingInterval;
    private Integer pingTimeout;
    private Integer port;
    private String hostname;
    private Long ttl;
}
