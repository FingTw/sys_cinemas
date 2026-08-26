package com.example.cinema.admin.services;

import com.example.cinema.admin.dto.BookingDetailResponse;
import com.example.cinema.admin.dto.BookingResponse;
import com.example.cinema.admin.dto.CreateBookingRequest;
import com.example.cinema.admin.dto.DashboardStatsDTO;
import java.util.List;

public interface AdminBookingUseCase {
    List<BookingDetailResponse> getAllBookings();
    BookingResponse createDirectBooking(CreateBookingRequest request, String staffId);
    DashboardStatsDTO getDashboardStats();
    void checkinBooking(String id);
}
