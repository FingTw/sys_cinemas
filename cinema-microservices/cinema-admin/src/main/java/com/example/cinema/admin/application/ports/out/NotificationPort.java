package com.example.cinema.admin.application.ports.out;

import com.example.cinema.common.events.BaseEvent;

public interface NotificationPort {
    void sendNotification(String topic, String key, BaseEvent payload);
}
