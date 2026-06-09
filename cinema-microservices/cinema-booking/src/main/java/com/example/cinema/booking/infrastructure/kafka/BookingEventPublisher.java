package com.example.cinema.booking.infrastructure.kafka;

import com.example.cinema.booking.domain.entities.Booking;
import com.example.cinema.common.events.BaseEvent;
import com.example.cinema.common.events.BookingConfirmedPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(BookingEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public BookingEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishBookingConfirmed(Booking booking,
                                       List<BookingConfirmedPayload.SeatInfo> seats,
                                       String movieTitle,
                                       String roomName,
                                       String email) {
        log.info("Preparing BOOKING_CONFIRMED event for booking: [{}]", booking.getId());
        try {
            BookingConfirmedPayload payload = BookingConfirmedPayload.builder()
                    .bookingId(booking.getId())
                    .userId(booking.getUserId())
                    .email(email)
                    .movieTitle(movieTitle)
                    .roomName(roomName)
                    .showtimeStart(booking.getExpiresAt().minusMinutes(5)) // Roughly start time or from showtime object
                    .seats(seats)
                    .totalPrice(booking.getTotalPrice())
                    .paymentTransactionId(booking.getPaymentTransactionId())
                    .qrCodeData("https://cinema.example.com/tickets/verify/" + booking.getId())
                    .build();

            BaseEvent<BookingConfirmedPayload> event = BaseEvent.create("BOOKING_CONFIRMED", payload);

            log.info("Publishing event BOOKING_CONFIRMED to Kafka topic [{}]", KafkaTopicConfig.BOOKING_EVENTS_TOPIC);
            kafkaTemplate.send(KafkaTopicConfig.BOOKING_EVENTS_TOPIC, booking.getId(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish booking confirmed event to Kafka for booking: [{}]", booking.getId(), ex);
                        } else {
                            log.info("Successfully published booking confirmed event to Kafka. Partition: [{}], Offset: [{}]",
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            log.error("Error creating or publishing booking confirmed event", e);
        }
    }
}
