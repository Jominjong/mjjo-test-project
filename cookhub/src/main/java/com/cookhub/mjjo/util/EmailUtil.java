package com.cookhub.mjjo.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * 이메일 발송 유틸리티 클래스
 * 
 * <p>Spring Mail을 사용하여 HTML 형식의 이메일을 발송하는 기능을 제공합니다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailUtil {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    /**
     * HTML 형식의 이메일을 발송합니다.
     * 
     * <p>UTF-8 인코딩을 사용하여 HTML 컨텐츠를 포함한 이메일을 발송합니다.
     * 발송 실패 시 MessagingException이 발생합니다.</p>
     * 
     * @param to 수신자 이메일 주소
     * @param subject 이메일 제목
     * @param htmlContent HTML 형식의 이메일 본문
     * @throws MessagingException 이메일 발송 실패 시
     * @throws IllegalArgumentException 매개변수가 null이거나 빈 값인 경우
     */
    public void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        validateEmailParameters(to, subject, htmlContent);

        try {
            MimeMessage mimeMessage = createMimeMessage(to, subject, htmlContent);
            mailSender.send(mimeMessage);
            log.debug("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw e;
        }
    }

    /**
     * MimeMessage 객체를 생성합니다.
     */
    private MimeMessage createMimeMessage(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        return mimeMessage;
    }

    /**
     * 이메일 발송 매개변수를 검증합니다.
     */
    private void validateEmailParameters(String to, String subject, String htmlContent) {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Recipient email address cannot be null or empty");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Email subject cannot be null or empty");
        }
        if (htmlContent == null || htmlContent.isBlank()) {
            throw new IllegalArgumentException("Email content cannot be null or empty");
        }
    }

}