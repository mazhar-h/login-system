package com.login.loginsystem.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class MimeMessageHelperFactory {

    public MimeMessageHelper create(MimeMessage mimeMessage) throws MessagingException {
        return new MimeMessageHelper(mimeMessage, true);
    }
}