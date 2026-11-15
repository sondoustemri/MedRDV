package com.medrdv.notification.service;

import com.medrdv.notification.dto.NotificationEmailDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void envoyerEmail(NotificationEmailDto dto) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("noreply@medrdv.com");
            helper.setTo(dto.destinataire());
            helper.setSubject(dto.objet());

            // Design HTML de l'email
            String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 8px; overflow: hidden;\">"
                    + "<div style=\"background-color: #007bff; color: white; padding: 20px; text-align: center;\">"
                    + "<h2>Med-RDV</h2>"
                    + "<p>Votre plateforme de prise de rendez-vous médicaux</p>"
                    + "</div>"
                    + "<div style=\"padding: 20px; color: #333;\">"
                    + "<p>Bonjour,</p>"
                    + "<p>" + dto.contenu() + "</p>"
                    + "<br>"
                    + "<p>Cordialement,</p>"
                    + "<p><strong>L'équipe Med-RDV</strong></p>"
                    + "</div>"
                    + "<div style=\"background-color: #f1f1f1; text-align: center; padding: 10px; font-size: 12px; color: #777;\">"
                    + "Cet email a été envoyé automatiquement. Merci de ne pas y répondre."
                    + "</div>"
                    + "</div>";

            helper.setText(htmlContent, true); // true = interpréter comme HTML

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }
}