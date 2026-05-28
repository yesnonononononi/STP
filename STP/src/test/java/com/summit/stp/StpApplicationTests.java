package com.summit.stp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class StpApplicationTests {

    @Autowired
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @Autowired
    private org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry registry;

    @Test
    void contextLoads() {
    }

    @Test
    void testSendMq() {
        com.summit.stp.payment.domain.event.PaySuccessEvent event = new com.summit.stp.payment.domain.event.PaySuccessEvent(2058208390793920512L);
        rabbitTemplate.convertAndSend("pay.exchange", "pay.queue.success", event);
        System.out.println("====== MQ Test Message Sent Successfully ======");
    }

    @Test
    void testRegistry() {
        System.out.println("====== Listener Container Count: " + registry.getListenerContainers().size() + " ======");
        for (org.springframework.amqp.rabbit.listener.MessageListenerContainer container : registry.getListenerContainers()) {
            System.out.println("Container: " + container + ", running: " + container.isRunning());
        }
    }

}

