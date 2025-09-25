package com.projectsaas.notification.service;

import com.projectsaas.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    private final TemplateService templateService;

    @Value("${notification.email.from}")
    private String fromEmail;

    public void sendEmail(Notification notification) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(notification.getRecipientEmail());
            helper.setSubject(notification.getTitle());

            String htmlContent = generateEmailContent(notification);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", notification.getRecipientEmail());

        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", notification.getRecipientEmail(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String generateEmailContent(Notification notification) {
        String templateName = getTemplateName(notification.getType());
        Context context = new Context();

        context.setVariable("title", notification.getTitle());
        context.setVariable("message", notification.getMessage());
        context.setVariable("type", notification.getType());

        if (notification.getMetadata() != null) {
            for (Map.Entry<String, String> entry : notification.getMetadata().entrySet()) {
                context.setVariable(entry.getKey(), entry.getValue());
            }
        }

        return templateEngine.process(templateName, context);
    }

    private String getTemplateName(com.projectsaas.notification.enums.NotificationType type) {
        return switch (type) {
            case TASK_ASSIGNED -> "email/task-assigned";
            case PROJECT_INVITATION -> "email/project-invitation";
            case DEADLINE_REMINDER -> "email/deadline-reminder";
            default -> "email/default";
        };
    }
}