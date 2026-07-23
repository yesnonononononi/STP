package com.summit.stp.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class StpGatewayApplication {
    public static void main(String[] args) {
        System.setProperty("nacos.server.grpc.port.offset", "1768");
        SpringApplication.run(StpGatewayApplication.class, args);
    }
}
