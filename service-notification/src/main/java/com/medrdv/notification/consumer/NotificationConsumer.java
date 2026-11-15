package com.medrdv.notification.consumer;

import com.medrdv.notification.dto.NotificationEmailDto;
import com.medrdv.notification.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private final EmailService emailService;

    public NotificationConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "rendezvous.notification.queue")
    public void receiveNotification(NotificationEmailDto message) {
        System.out.println("==================================================");
        System.out.println("📩 MESSAGE REÇU POUR : " + message.destinataire());
        emailService.envoyerEmail(message);
        System.out.println("📧 Email envoyé avec succès via Mailpit !");
        System.out.println("==================================================");
    }
}