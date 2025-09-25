package com.projectsaas.notification.service;

import com.projectsaas.notification.entity.NotificationTemplate;
import com.projectsaas.notification.enums.NotificationType;
import com.projectsaas.notification.exception.TemplateNotFoundException;
import com.projectsaas.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {

    private final NotificationTemplateRepository templateRepository;

    public NotificationTemplate getTemplateByType(NotificationType type) {
        return templateRepository.findByTypeAndActive(type, true)
                .orElseThrow(() -> new TemplateNotFoundException("No active template found for type: " + type));
    }

    public NotificationTemplate getTemplateByName(String name) {
        return templateRepository.findByNameAndActive(name, true)
                .orElseThrow(() -> new TemplateNotFoundException("No active template found with name: " + name));
    }

    public List<NotificationTemplate> getAllActiveTemplates() {
        return templateRepository.findAll().stream()
                .filter(NotificationTemplate::isActive)
                .toList();
    }

    public NotificationTemplate createTemplate(NotificationTemplate template) {
        log.info("Creating new notification template: {}", template.getName());
        return templateRepository.save(template);
    }

    public NotificationTemplate updateTemplate(Long id, NotificationTemplate template) {
        NotificationTemplate existing = templateRepository.findById(id)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found with id: " + id));

        existing.setSubject(template.getSubject());
        existing.setBodyTemplate(template.getBodyTemplate());
        existing.setHtmlTemplate(template.getHtmlTemplate());
        existing.setActive(template.isActive());

        log.info("Updating notification template: {}", existing.getName());
        return templateRepository.save(existing);
    }

    public void deactivateTemplate(Long id) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found with id: " + id));

        template.setActive(false);
        templateRepository.save(template);
        log.info("Deactivated notification template: {}", template.getName());
    }
}