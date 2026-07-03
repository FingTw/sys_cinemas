package com.example.cinema.booking.application.ports.in;

import com.example.cinema.booking.application.dto.BookingResponse;
import com.example.cinema.booking.application.dto.CreateBookingRequest;
import com.example.cinema.booking.application.dto.SeatStatusDTO;
import java.util.List;

public interface BookingService {
    BookingResponse createBooking(CreateBookingRequest request, String userId, String ipAddress);
    void confirmPayment(String bookingId, String transactionId);
    List<SeatStatusDTO> getSeatStatusesByShowtime(String showtimeId);

    // Camunda Helpers
    BookingResponse createPendingBooking(String showtimeId, List<String> seatIds, List<com.example.cinema.booking.application.dto.BookingItemRequest> items, String userId);
    void confirmBookingStatus(String bookingId, String transactionId);
    void cancelPendingBooking(String bookingId);
    void publishBookingConfirmedEvent(String bookingId);
    void refundBooking(String bookingId);
}
