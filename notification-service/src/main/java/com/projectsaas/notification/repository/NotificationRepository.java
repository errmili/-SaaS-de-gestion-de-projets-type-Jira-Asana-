package com.projectsaas.notification.repository;

import com.projectsaas.notification.entity.Notification;
import com.projectsaas.notification.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    List<Notification> findByStatusAndScheduledForBefore(NotificationStatus status, LocalDateTime dateTime);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.status != 'READ'")
    Long countUnreadByUserId(@Param("userId") Long userId);

    List<Notification> findByUserIdAndStatus(Long userId, NotificationStatus status);

    List<Notification> findByCreatedAtBefore(LocalDateTime dateTime);
}
