package com.summit.stp.gateway.config;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

@Component
@Slf4j
@RequiredArgsConstructor
public class DynamicRouteLoader {

    private final RouteDefinitionWriter routeDefinitionWriter;
    @Value("${spring.application.name}")
    private String dataId;
    @Value("${spring.cloud.nacos.config.group}")
    private String group;
    private final NacosConfigManager nacosConfigManager;
    Set<String> routes = new HashSet<>();


    @PostConstruct
    public void initRouteListener() throws NacosException {
        final int timeoutMs = 5000;
        String configInfo = nacosConfigManager.getConfigService().getConfigAndSignListener(dataId+".json", group, timeoutMs, new Listener() {
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                updateRoute(configInfo);
            }
        });
        updateRoute(configInfo);
    }

    private void updateRoute(String configInfo) {
        String info = "";
        if(configInfo != null ) info = configInfo.substring(0, Math.min(configInfo.length(), 100));
        log.info("【网关-路由消息变更】更新路由信息: {}", info);
        //1, 获取路由信息
        List<RouteDefinition> definitionList = JSONUtil.toList(configInfo, RouteDefinition.class);

        //2, 删除所有路由
        for (String routeId : routes) {
            routeDefinitionWriter.delete(Mono.just(routeId)).subscribe();
        }

        //3, 清空路由缓存
        routes.clear();

        //4, 更新路由
        for (RouteDefinition routeDefinition : definitionList) {
            routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
            routes.add(routeDefinition.getId());
        }
        log.info("【网关-路由消息变更】更新路由成功:{}", info);
    }
}
