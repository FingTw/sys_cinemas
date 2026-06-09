package com.example.cinema.booking.application.ports.out;

import com.example.cinema.booking.domain.entities.Booking;
import com.example.cinema.common.events.BookingConfirmedPayload;
import java.util.List;

public interface EventPublisherPort {
    void publishBookingConfirmed(Booking booking,
                                 List<BookingConfirmedPayload.SeatInfo> seats,
                                 String movieTitle,
                                 String roomName,
                                 String email);
}
