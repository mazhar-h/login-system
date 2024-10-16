package com.login.loginsystem.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mail.javamail.MimeMessageHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MimeMessageHelperFactoryTest {

    private final MimeMessageHelperFactory factory = new MimeMessageHelperFactory();

    @Test
    void testCreate_Success() throws MessagingException {
        // Arrange
        MimeMessage mimeMessage = Mockito.mock(MimeMessage.class);

        // Act
        MimeMessageHelper helper = factory.create(mimeMessage);

        // Assert
        assertThat(helper).isNotNull();
    }
}