package com.example.cinema.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseEvent<T> {
    private String eventId;
    private String eventType;
    private ZonedDateTime timestamp;
    private T payload;

    public static <T> BaseEvent<T> create(String eventType, T payload) {
        BaseEvent<T> event = new BaseEvent<>();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(eventType);
        event.setTimestamp(ZonedDateTime.now());
        event.setPayload(payload);
        return event;
    }
}
