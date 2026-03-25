package com.motorplus.motorplus.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Código de verificación - Motor Plus");
        message.setText(
                "Hola,\n\n" +
                "Has sido registrado como administrador en Motor Plus.\n\n" +
                "Tu código de verificación es:\n\n" +
                "    " + code + "\n\n" +
                "Ingresa este código en la aplicación para activar tu cuenta.\n" +
                "El código expira en 15 minutos.\n\n" +
                "Si no solicitaste este registro, ignora este mensaje.\n\n" +
                "Motor Plus"
        );

        mailSender.send(message);
    }
}
