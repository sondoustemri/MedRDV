package com.medrdv.servicerendezvous.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String QUEUE_NAME = "rendezvous.notification.queue";

    @Bean
    public Queue notificationQueue() {
        return new Queue(QUEUE_NAME, false);
    }
}