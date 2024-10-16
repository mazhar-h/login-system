package com.login.loginsystem.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private MimeMessage mimeMessage;
    @Mock
    private MimeMessageHelper mimeMessageHelper;
    @Mock
    private MimeMessageHelperFactory mimeMessageHelperFactory;
    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendEmail_Success() throws MessagingException {
        // Arrange
        String to = "test@example.com";
        String subject = "Test Subject";
        String content = "Test Content";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(mimeMessageHelperFactory.create(mimeMessage)).thenReturn(mimeMessageHelper);

        // Act
        emailService.sendEmail(to, subject, content);

        // Assert
        verify(mailSender).send(mimeMessage); // Verify that mailSender's send method was called
        verify(mimeMessageHelper).setTo(to); // Verify that recipient was set
        verify(mimeMessageHelper).setSubject(subject); // Verify that subject was set
        verify(mimeMessageHelper).setText(content, true); // Verify that content was set with HTML enabled
    }

    @Test
    void testSendEmail_MailException() throws MessagingException {
        // Arrange
        String to = "test@example.com";
        String subject = "Test Subject";
        String content = "Test Content";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(mimeMessageHelperFactory.create(mimeMessage)).thenReturn(mimeMessageHelper);
        doThrow(new MailException("Simulated Messaging Exception") {})
                .when(mailSender).send(mimeMessage);

        // Act & Assert
        assertThatThrownBy(() -> emailService.sendEmail(to, subject, content))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to send email due to MailException");
    }

    @Test
    void testSendEmail_MessagingException() throws Exception {
        // Arrange
        String to = "test@example.com";
        String subject = "Test Subject";
        String content = "Test Content";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(mimeMessageHelperFactory.create(mimeMessage)).thenReturn(mimeMessageHelper);
        doThrow(new MessagingException("Simulated Messaging Exception"))
                .when(mimeMessageHelper).setSubject(anyString());

        // Act and Assert
        assertThatThrownBy(() -> emailService.sendEmail(to, subject, content))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("MessagingException");
    }
}