package com.scrip.msnotificaciones.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class SmtpEmailClient {
    private final JavaMailSender mailSender;
    private final String from;

    public SmtpEmailClient(JavaMailSender mailSender, @Value("${notifications.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public String enviar(String destino, String asunto, String html) throws Exception {
        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, "UTF-8");
        helper.setFrom(from);
        helper.setTo(destino);
        helper.setSubject(asunto);
        helper.setText(html, true);
        mailSender.send(mensaje);
        return mensaje.getMessageID();
    }
}
