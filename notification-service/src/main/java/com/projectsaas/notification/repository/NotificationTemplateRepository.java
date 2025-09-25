package com.projectsaas.notification.repository;

import com.projectsaas.notification.entity.NotificationTemplate;
import com.projectsaas.notification.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByNameAndActive(String name, boolean active);

    Optional<NotificationTemplate> findByTypeAndActive(NotificationType type, boolean active);
}