package com.projectsaas.analytics.kafka.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DebugKafkaListener {

    @KafkaListener(
            topics = {"analytics.*"},
            groupId = "analytics-debug",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleAnalyticsEvents(
            @Payload Object payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.debug("Analytics Event Received - Topic: {}, Partition: {}, Offset: {}, Payload: {}",
                topic, partition, offset, payload);
    }

    // Listener pour capturer tous les événements entrants (pour debug)
    @KafkaListener(
            topicPattern = ".*",
            groupId = "analytics-all-events-debug",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleAllEvents(
            @Payload Object payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        if (log.isTraceEnabled()) {
            log.trace("All Events Debug - Topic: {}, Payload: {}", topic, payload);
        }
    }
}