package com.summit.stp.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.StatelessRetryOperationsInterceptor;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;


@Slf4j
@EnableRabbit
@Configuration
public class MqConfig {
    @Value("${spring.rabbitmq.host:localhost}")
    private String rabbitmqHost;
    @Value("${spring.rabbitmq.port:5672}")
    private int rabbitmqPort;
    @Value("${spring.rabbitmq.username:guest}")
    private String username;
    @Value("${spring.rabbitmq.password:guest}")
    private String password;
    @Value("${spring.rabbitmq.max-retries:3}")
    private int maxRetries;
    @Bean
    public ConnectionFactory connectionFactory(){
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(rabbitmqHost);
        cachingConnectionFactory.setPort(rabbitmqPort);
        cachingConnectionFactory.setUsername(username);
        cachingConnectionFactory.setPassword(password);
        //启用发布确认
        cachingConnectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        // 启用发布返回（Publisher Returns）：
        // 当消息无法路由到任何队列时（例如交换机存在但绑定的队列不存在，或 routingKey 不匹配），
        // RabbitMQ 会将消息返回给生产者。
        // 注意：要接收返回的消息，还需要在 RabbitTemplate 中设置 ReturnCallback。
        cachingConnectionFactory.setPublisherReturns(true);
        return cachingConnectionFactory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public MessageConverter messageConverter(JsonMapper jsonMapper){
        return new ToolsJacksonMessageConverter(jsonMapper);
    }


    @Bean
    public RabbitTemplate rabbitTemplate(MessageConverter messageConverter){

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());

        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setUseDirectReplyToContainer(true);

        //强制消息投递
        rabbitTemplate.setMandatory(true);

        // 确认回调 (ConfirmCallback)
        // 执行时机：当消息成功到达 RabbitMQ Broker（交换机）后触发。
        // - ack=true: 消息已成功被交换机接收。
        // - ack=false: 消息未被交换机接收（例如交换机不存在），cause 包含失败原因。
        // 注意：这仅保证消息到达交换机，不保证消息被路由到队列或被消费者消费。
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("消息已到达交换机 (Ack): CorrelationId={}, Cause={}",
                        correlationData != null ? correlationData.getId() : "null",
                        cause);
            } else {
                log.error("消息未到达交换机 (Nack): CorrelationId={}, Cause={}",
                        correlationData != null ? correlationData.getId() : "null",
                        cause);
            }
        });

        // 消息退回回调 (ReturnsCallback)
        // 执行时机：当消息成功到达交换机，但没有匹配的队列时触发（通常因为 routingKey 错误或队列不存在）
        rabbitTemplate.setReturnsCallback(returned -> {
            log.error("消息路由失败退回 (Return): replyCode={}, replyText={}, exchange={}, routingKey={}, message={}",
                    returned.getReplyCode(),
                    returned.getReplyText(),
                    returned.getExchange(),
                    returned.getRoutingKey(),
                    returned.getMessage());
        });

        return rabbitTemplate;
    }


    //监听器容器工厂
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(MessageConverter messageConverter, StatelessRetryOperationsInterceptor statelessRetryOperationsInterceptor){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setMessageConverter(messageConverter);


        //设置并发消费者数量
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);

        //每次从队列获取的消息数量
        factory.setPrefetchCount(1);

        //手动确认
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        //重试拦截器
        factory.setAdviceChain(statelessRetryOperationsInterceptor);
        return factory;
    }


    //重试拦截器
    @Bean
    public StatelessRetryOperationsInterceptor statelessRetryOperationsInterceptor(RabbitTemplate rabbitTemplate){
        return RetryInterceptorBuilder.stateless()
                .maxRetries(maxRetries)
                .backOffOptions(1000,2.0,10000)
                .recoverer(new RepublishMessageRecoverer(
                        rabbitTemplate,
                        "pay.exchange",      // 指定用于转发失败消息的交换机名称
                        "pay.queue.fail.recoverer" // 指定路由键，消息将被路由到绑定此路由键的死信/错误队列
                )).build();

    }

    // ==================== 1. 支付相关 Exchange, Queue & Binding ====================
    @Bean
    public DirectExchange payExchange() {
        return new DirectExchange("pay.exchange", true, false);
    }

    @Bean
    public Queue paySuccessQueue() {
        return QueueBuilder.durable("pay.queue.success")
                .deadLetterExchange("pay.exchange")
                .deadLetterRoutingKey("pay.queue.fail.recoverer")
                .build();
    }

    @Bean
    public Queue payFailQueue() {
        return QueueBuilder.durable("pay.queue.fail")
                .deadLetterExchange("pay.exchange")
                .deadLetterRoutingKey("pay.queue.fail.recoverer")
                .build();
    }

    @Bean
    public Queue payFailRecovererQueue() {
        return QueueBuilder.durable("pay.queue.fail.recoverer").build();
    }

    @Bean
    public Binding paySuccessBinding(Queue paySuccessQueue, DirectExchange payExchange) {
        return BindingBuilder.bind(paySuccessQueue).to(payExchange).with("pay.queue.success");
    }

    @Bean
    public Binding payFailBinding(Queue payFailQueue, DirectExchange payExchange) {
        return BindingBuilder.bind(payFailQueue).to(payExchange).with("pay.queue.fail");
    }

    @Bean
    public Binding payFailRecovererBinding(Queue payFailRecovererQueue, DirectExchange payExchange) {
        return BindingBuilder.bind(payFailRecovererQueue).to(payExchange).with("pay.queue.fail.recoverer");
    }

    // ==================== 2. 会员权益相关 Exchange, Queue & Binding ====================
    @Bean
    public DirectExchange memberExchange() {
        return new DirectExchange("member.exchange.pay", true, false);
    }

    @Bean
    public Queue memberPayQueue() {
        return QueueBuilder.durable("member.queue.pay").build();
    }

    @Bean
    public Binding memberPayBinding(Queue memberPayQueue, DirectExchange memberExchange) {
        return BindingBuilder.bind(memberPayQueue).to(memberExchange).with("member.queue.pay");
    }

    // ==================== 3. 用户注册相关 Exchange, Queue & Binding ====================
    @Bean
    public DirectExchange userExchange() {
        return new DirectExchange("user.exchange.register", true, false);
    }

    @Bean
    public Queue userRegisterQueue() {
        return QueueBuilder.durable("user.queue.register").build();
    }

    @Bean
    public Binding userRegisterBinding(Queue userRegisterQueue, DirectExchange userExchange) {
        return BindingBuilder.bind(userRegisterQueue).to(userExchange).with("user.queue.register");
    }

    static class ToolsJacksonMessageConverter implements MessageConverter {
        private final JsonMapper jsonMapper;

        public ToolsJacksonMessageConverter(JsonMapper jsonMapper) {
            this.jsonMapper = jsonMapper;
        }

        @Override
        public Message toMessage(Object object, MessageProperties messageProperties) throws MessageConversionException {
            try {
                byte[] bytes = jsonMapper.writeValueAsBytes(object);
                messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
                messageProperties.setContentLength(bytes.length);
                messageProperties.getHeaders().put("__TypeId__", object.getClass().getName());
                return new Message(bytes, messageProperties);
            } catch (Exception e) {
                throw new MessageConversionException("Failed to serialize object to JSON", e);
            }
        }

        @Override
        public Object fromMessage(Message message) throws MessageConversionException {
            MessageProperties properties = message.getMessageProperties();
            if (properties == null || !MessageProperties.CONTENT_TYPE_JSON.equals(properties.getContentType())) {
                return message.getBody();
            }
            Object typeIdObj = properties.getHeaders().get("__TypeId__");
            if (typeIdObj == null) {
                return new String(message.getBody());
            }
            String typeId = typeIdObj.toString();
            try {
                Class<?> targetClass = Class.forName(typeId);
                return jsonMapper.readValue(message.getBody(), targetClass);
            } catch (Exception e) {
                log.error("【MQ反序列化】解析 JSON 消息失败! Class: {}, 消息体: {}", typeId, new String(message.getBody()), e);
                throw new MessageConversionException("Failed to deserialize JSON message", e);
            }
        }
    }


    // =================================4,帖子相关==================================
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange("post.topic.exchange", true, false);
    }

    //帖子 - 用户 队列
    @Bean
    public Queue postQueue() {
        return QueueBuilder.durable("post.user.queue").build();
    }

    //帖子 - 用户 绑定
    @Bean
    public Binding postUserBinding(Queue postQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(postQueue).to(topicExchange).with("post.user.queue");
    }


}
