package com.example.cinema.booking.adapter.messaging;

import com.example.cinema.booking.application.port.BookingEventPublisherPort;
import com.example.cinema.booking.domain.Booking;
import com.example.cinema.common.events.BookingConfirmedPayload;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingEventPublisherAdapter implements BookingEventPublisherPort {
    private final BookingEventPublisher eventPublisher;

    @Override
    public void publishBookingConfirmed(Booking booking, List<BookingConfirmedPayload.SeatInfo> seatInfos, String movieTitle, String roomName, String email) {
        eventPublisher.publishBookingConfirmed(booking, seatInfos, movieTitle, roomName, email);
    }
}
