package com.summit.stp.shared.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MqConfig {

    @Bean
    public RabbitTemplate rabbitTemplate(ApplicationContext applicationContext){
        org.springframework.amqp.rabbit.connection.CachingConnectionFactory connectionFactory = 
                new org.springframework.amqp.rabbit.connection.CachingConnectionFactory("localhost");
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setApplicationContext(applicationContext);
        rabbitTemplate.setUseDirectReplyToContainer(true);
        return rabbitTemplate;
    }



}
