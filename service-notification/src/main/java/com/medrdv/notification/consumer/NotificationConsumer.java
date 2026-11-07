package com.medrdv.notification.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    @RabbitListener(queues = "rendezvous.notification.queue")
    public void receiveNotification(String message) {
        System.out.println("==================================================");
        System.out.println("📩 NOTIFICATION REÇUE : " + message);
        System.out.println("📧 Envoi simulé d'un e-mail au patient...");
        System.out.println("==================================================");
    }
}