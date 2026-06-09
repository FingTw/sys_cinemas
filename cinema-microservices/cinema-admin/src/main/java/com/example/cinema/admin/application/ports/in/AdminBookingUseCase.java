package com.example.cinema.admin.application.ports.in;

import com.example.cinema.admin.application.dto.BookingDetailResponse;
import com.example.cinema.admin.application.dto.BookingResponse;
import com.example.cinema.admin.application.dto.CreateBookingRequest;
import com.example.cinema.admin.application.dto.DashboardStatsDTO;
import java.util.List;

public interface AdminBookingUseCase {
    List<BookingDetailResponse> getAllBookings();
    BookingResponse createDirectBooking(CreateBookingRequest request, String staffId);
    DashboardStatsDTO getDashboardStats();
    void checkinBooking(String id);
}
