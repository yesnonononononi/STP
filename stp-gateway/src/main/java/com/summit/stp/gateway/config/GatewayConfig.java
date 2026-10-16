package com.summit.stp.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    /**
     * 内部 RPC 接口路径标识，命中即拒绝外部访问。
     * 与 AuthorizeGatewayFilter 中的同名语义常量保持一致。
     */
    private static final String INTERNAL_PATH_SEGMENT = "/internal/";

    private final StpGatewayProperties gatewayProperties;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        log.info("【网关】开始从配置文件初始化动态路由");
        RouteLocatorBuilder.Builder routesBuilder = builder.routes();
        
        // 核心安全策略：拦截任何外部直接访问含有 /internal/ 的接口请求，统一返回 403。
        // 说明：Spring Boot 4 / Spring Framework 7 默认启用 PathPatternParser，其语法要求
        // 通配符 "**" 只能出现在路径末尾，因此形如 "/**/internal/**" 的路径谓词属非法模式，
        // 会导致应用启动即失败（Invalid mapping pattern detected）。
        // 这里改用自定义同步谓词 Predicate<ServerWebExchange>，直接判断请求路径是否包含
        // "/internal/"，既规避非法路径模式，又保持"路径含 /internal/ 即 403"的原始语义。
        // 同时 AuthorizeGatewayFilter 中已补充基于路径的 GlobalFilter 兜底，二者互为补充。
        routesBuilder.route("block-internal-apis", r -> r.predicate(exchange -> {
                    String path = exchange.getRequest().getURI().getPath();
                    return path != null && path.contains(INTERNAL_PATH_SEGMENT);
                })
                .filters(f -> f.setStatus(HttpStatus.FORBIDDEN))
                .uri("no://op"));

        for (StpGatewayProperties.RouteConfig route : gatewayProperties.getRoutes()) {
            log.info("【网关】注册路由 - ID: {}, URI: {}, Paths: {}", route.getId(), route.getUri(), route.getPaths());
            String[] pathArray = route.getPaths().toArray(new String[0]);
            // 剥离任何外部伪造的身份透传头与内部调用标记，确保内部安全
            routesBuilder.route(route.getId(), r -> r.path(pathArray)
                    .filters(f -> f.removeRequestHeader("X-Internal-Request")
                            .removeRequestHeader("X-User-Id")
                            .removeRequestHeader("X-User-Name")
                            .removeRequestHeader("X-User-Admin")
                            .removeRequestHeader("X-User-IP")
                            .removeRequestHeader("X-User-Token")
                            .removeRequestHeader("X-User-Token-Type")
                            .removeRequestHeader("X-User-Session"))
                    .uri(route.getUri()));
        }
        
        RouteLocator routes = routesBuilder.build();
        log.info("【网关】动态路由配置加载完成，数量: {}", gatewayProperties.getRoutes().size());
        return routes;
    }
}
