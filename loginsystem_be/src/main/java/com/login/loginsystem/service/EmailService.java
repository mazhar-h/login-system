package com.login.loginsystem.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final MimeMessageHelperFactory mimeMessageHelperFactory;

    public EmailService(JavaMailSender mailSender, MimeMessageHelperFactory mimeMessageHelperFactory) {
        this.mailSender = mailSender;
        this.mimeMessageHelperFactory = mimeMessageHelperFactory;
    }

    public void sendEmail(String to, String subject, String content) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = mimeMessageHelperFactory.create(mimeMessage);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(mimeMessage);
        }  catch (MailException e) {
            throw new RuntimeException("Failed to send email due to MailException", e);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}