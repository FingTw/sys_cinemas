package com.example.cinema.booking.application.port;

import com.example.cinema.booking.domain.Booking;
import com.example.cinema.common.events.BookingConfirmedPayload;
import java.util.List;

public interface BookingEventPublisherPort {
    void publishBookingConfirmed(Booking booking, List<BookingConfirmedPayload.SeatInfo> seatInfos, String movieTitle, String roomName, String email);
}
