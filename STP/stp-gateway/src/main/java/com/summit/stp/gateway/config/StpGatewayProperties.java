package com.summit.stp.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "stp.gateway")
public class StpGatewayProperties {
    private List<RouteConfig> routes = new ArrayList<>();

    @Data
    public static class RouteConfig {
        private String id;
        private String uri;
        private List<String> paths = new ArrayList<>();
    }
}
