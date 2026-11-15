package com.medrdv.servicerendezvous.publisher;

import com.medrdv.servicerendezvous.config.RabbitMQConfig;
import com.medrdv.servicerendezvous.dto.NotificationEmailDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public NotificationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendNotification(NotificationEmailDto message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, message);
        System.out.println("🚀 Message publié vers RabbitMQ : " + message.objet());
    }
}