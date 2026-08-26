package com.example.cinema.admin.adapters;

import com.example.cinema.admin.services.NotificationPort;
import com.example.cinema.common.events.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaNotificationAdapter implements NotificationPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void sendNotification(String topic, String key, BaseEvent payload) {
        try {
            if (key != null) {
                kafkaTemplate.send(topic, key, payload);
            } else {
                kafkaTemplate.send(topic, payload);
            }
            log.info("KafkaNotificationAdapter: Sent event to topic [{}] with key [{}]", topic, key);
        } catch (Exception e) {
            log.error("KafkaNotificationAdapter: Failed to send event to topic [{}]: {}", topic, e.getMessage(), e);
        }
    }
}
