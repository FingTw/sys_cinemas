package com.example.cinema.booking.infrastructure.adapters;

import com.example.cinema.booking.application.ports.out.EventPublisherPort;
import com.example.cinema.booking.domain.entities.Booking;
import com.example.cinema.booking.infrastructure.kafka.BookingEventPublisher;
import com.example.cinema.common.events.BookingConfirmedPayload;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingKafkaAdapter implements EventPublisherPort {

    private final BookingEventPublisher bookingEventPublisher;

    @Override
    public void publishBookingConfirmed(Booking booking, List<BookingConfirmedPayload.SeatInfo> seats, String movieTitle, String roomName, String email) {
        bookingEventPublisher.publishBookingConfirmed(booking, seats, movieTitle, roomName, email);
    }
}
