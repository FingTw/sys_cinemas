package com.example.cinema.application.ports.in;

import com.example.cinema.application.dto.BookingDetailResponse;
import java.util.List;

/**
 * Port cho các thao tác đọc/hủy booking từ phía User và Admin.
 */
public interface BookingQueryUseCase {
    // User: Xem lịch sử đặt vé
    List<BookingDetailResponse> getMyBookings(String userId);

    // User: Xem chi tiết 1 booking (chỉ cho phép xem booking của mình)
    BookingDetailResponse getBookingDetail(String bookingId, String userId);

    // User: Hủy booking PENDING
    void cancelBooking(String bookingId, String userId);

    // Admin: Xem tất cả booking
    List<BookingDetailResponse> getAllBookings();
}
