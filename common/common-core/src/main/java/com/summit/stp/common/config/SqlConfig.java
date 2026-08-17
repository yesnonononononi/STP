package com.summit.stp.common.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class SqlConfig {

    @Value("${mybatis-plus.mapper-locations:classpath*:/mapper/**/*.xml}")
    private String mapperLocations;


    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource, ObjectProvider<MetaObjectHandler> metaObjectHandlerProvider) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);

        // 配置 MyBatis-Plus 属性
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        factoryBean.setConfiguration(configuration);

        // 配置全局属性以使 MetaObjectHandler 生效
        GlobalConfig globalConfig = new GlobalConfig();
        metaObjectHandlerProvider.ifAvailable(globalConfig::setMetaObjectHandler);
        factoryBean.setGlobalConfig(globalConfig);

        // 从配置读取 mapper-locations，支持逗号分隔的多路径扫描
        List<Resource> resourceList = new ArrayList<>();
        if (mapperLocations != null && !mapperLocations.trim().isEmpty()) {
            String[] locations = mapperLocations.split(",");
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            for (String location : locations) {
                if (location != null && !location.trim().isEmpty()) {
                    try {
                        Resource[] resources = resolver.getResources(location.trim());
                        resourceList.addAll(Arrays.asList(resources));
                    } catch (IOException e) {
                        // 忽略解析失败的路径
                    }
                }
            }
        }
        factoryBean.setMapperLocations(resourceList.toArray(new Resource[0]));

        return factoryBean.getObject();
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
