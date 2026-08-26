package com.example.cinema.admin.services;

import com.example.cinema.common.events.BaseEvent;

public interface NotificationPort {
    void sendNotification(String topic, String key, BaseEvent payload);
}
