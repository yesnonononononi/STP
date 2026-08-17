package com.summit.stp.operationlog.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan(basePackages = "com.summit.stp.operationlog")
@MapperScan(basePackages = "com.summit.stp.operationlog.infrastructure.persistence.mapper")
public class OperationLogAutoConfiguration {
}
