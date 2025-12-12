package fr.gamegauge.gamegauge_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Attention : Mettez votre vrai email d'envoi ici ou via une variable
    @Value("${spring.mail.username}")
    private String senderEmail;

    public void sendResetToken(String userEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(userEmail);
        message.setSubject("GameGauge - Réinitialisation de mot de passe");
        // Le lien pointe vers le Frontend Angular
        message.setText("Pour réinitialiser votre mot de passe, cliquez ici :\n"
                + "https://gamegauge.fr/reset-password?token=" + token);

        mailSender.send(message);
    }
}
