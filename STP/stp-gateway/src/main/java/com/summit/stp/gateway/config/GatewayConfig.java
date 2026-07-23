package com.summit.stp.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final StpGatewayProperties gatewayProperties;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        log.info("【网关】开始从配置文件初始化动态路由");
        RouteLocatorBuilder.Builder routesBuilder = builder.routes();
        
        // 核心安全策略：拦截任何外部直接访问含有 /internal/ 的接口请求，统一返回 403
        routesBuilder.route("block-internal-apis", r -> r.path("/*/internal/**")
                .filters(f -> f.setStatus(HttpStatus.FORBIDDEN))
                .uri("no://op"));
        
        for (StpGatewayProperties.RouteConfig route : gatewayProperties.getRoutes()) {
            log.info("【网关】注册路由 - ID: {}, URI: {}, Paths: {}", route.getId(), route.getUri(), route.getPaths());
            String[] pathArray = route.getPaths().toArray(new String[0]);
            // 剥离任何外部伪造的内部透传 Header，确保内部安全
            routesBuilder.route(route.getId(), r -> r.path(pathArray)
                    .filters(f -> f.removeRequestHeader("X-Internal-Request"))
                    .uri(route.getUri()));
        }
        
        RouteLocator routes = routesBuilder.build();
        log.info("【网关】动态路由配置加载完成，数量: {}", gatewayProperties.getRoutes().size());
        return routes;
    }
}
