package com.example.ecommerce.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    public void sendResetPasswordEmail(String to, String resetLink) {
        try {
            Context context = new Context();
            context.setVariable("resetLink", resetLink);

            String htmlContent = templateEngine.process("email/reset-password", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("【電商平台】重設您的密碼");
            helper.setText(htmlContent, true);  // true = isHtml

            mailSender.send(message);
            log.info("密碼重設郵件已成功發送至: {}", to);

        } catch (MessagingException e) {
            log.error("郵件格式錯誤或發送失敗", e);
            throw new RuntimeException("郵件發送失敗：訊息格式錯誤", e);
        } catch (Exception e) {
            log.error("發送密碼重設郵件失敗，收件人: {}", to, e);
            // 不要把 UnknownHostException 暴露給前端
            throw new RuntimeException("目前無法寄送郵件，請稍後再試", e);
        }
    }
}