package com.example.cinema.booking.application.usecases;

import com.example.cinema.booking.application.dto.BookingDetailResponse;
import com.example.cinema.booking.application.dto.feign.SeatDTO;
import com.example.cinema.booking.application.dto.feign.ShowtimeDTO;
import com.example.cinema.booking.application.ports.in.BookingQueryUseCase;
import com.example.cinema.booking.domain.entities.Booking;
import com.example.cinema.booking.domain.repositories.BookingRepository;
import com.example.cinema.booking.infrastructure.feign.FacilityClient;
import com.example.cinema.booking.infrastructure.feign.ShowtimeClient;
import com.example.cinema.common.exception.ClientException;
import com.example.cinema.common.exception.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingQueryService implements BookingQueryUseCase {

    private static final Logger log = LoggerFactory.getLogger(BookingQueryService.class);

    private final BookingRepository bookingRepository;
    private final ShowtimeClient showtimeClient;
    private final FacilityClient facilityClient;

    public BookingQueryService(BookingRepository bookingRepository,
                               ShowtimeClient showtimeClient,
                               FacilityClient facilityClient) {
        this.bookingRepository = bookingRepository;
        this.showtimeClient = showtimeClient;
        this.facilityClient = facilityClient;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDetailResponse> getMyBookings(String userId) {
        log.info("Truy van lich su dat ve cho User: [{}]", userId);
        try {
            List<Booking> bookings = bookingRepository.findByUserId(userId);
            return bookings.stream()
                    .map(b -> enrichBooking(b, null))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Loi khi truy van lich su dat ve cua User [{}]: {}", userId, e.getMessage(), e);
            throw new ServerException("Loi he thong khi truy van lich su dat ve: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDetailResponse getBookingDetail(String bookingId, String userId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ClientException("Khong tim thay don dat ve ID: " + bookingId));

            if (!booking.getUserId().equals(userId)) {
                throw new ClientException("Ban khong co quyen xem don dat ve nay.");
            }

            return enrichBooking(booking, null);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Loi khi truy van chi tiet Booking [{}]: {}", bookingId, e.getMessage(), e);
            throw new ServerException("Loi he thong khi truy van chi tiet don dat ve: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void cancelBooking(String bookingId, String userId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ClientException("Khong tim thay don dat ve ID: " + bookingId));

            if (!booking.getUserId().equals(userId)) {
                throw new ClientException("Ban khong co quyen huy don dat ve nay.");
            }

            if (!"PENDING".equals(booking.getStatus())) {
                throw new ClientException("Chi co the huy don dat ve dang cho thanh toan (PENDING).");
            }

            bookingRepository.deleteById(bookingId);
            log.info("Da huy Booking ID: [{}]", bookingId);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Loi khi huy Booking [{}]: {}", bookingId, e.getMessage(), e);
            throw new ServerException("Loi he thong khi huy don dat ve: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDetailResponse> getAllBookings() {
        try {
            List<Booking> bookings = bookingRepository.findAll();
            return bookings.stream()
                    .map(b -> enrichBooking(b, null))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Loi khi truy van toan bo booking: {}", e.getMessage(), e);
            throw new ServerException("Loi he thong khi truy van danh sach don dat ve: " + e.getMessage(), e);
        }
    }

    private BookingDetailResponse enrichBooking(Booking booking, String usernameOverride) {
        ShowtimeDTO showtime = booking.getShowtimeId() != null
                ? showtimeClient.getShowtimeById(booking.getShowtimeId()).orElse(null)
                : null;

        List<BookingDetailResponse.SeatInfo> seatInfos = Collections.emptyList();
        if (booking.getSeats() != null) {
            seatInfos = booking.getSeats().stream().map(bs -> {
                SeatDTO seat = facilityClient.getSeatById(bs.getSeatId()).orElse(null);
                return new BookingDetailResponse.SeatInfo(
                        bs.getSeatId(),
                        seat != null ? seat.getRowLabel() : "?",
                        seat != null ? seat.getColNumber() : 0,
                        seat != null ? seat.getType() : "STANDARD",
                        bs.getPrice()
                );
            }).collect(Collectors.toList());
        }

        return BookingDetailResponse.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .username(usernameOverride)
                .movieTitle(showtime != null ? showtime.getMovieTitle() : "N/A")
                .roomName(showtime != null ? showtime.getRoomName() : "N/A")
                .showtimeStart(showtime != null ? showtime.getStartTime() : null)
                .showtimeEnd(showtime != null ? showtime.getEndTime() : null)
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .expiresAt(booking.getExpiresAt())
                .paymentTransactionId(booking.getPaymentTransactionId())
                .createdAt(booking.getCreatedAt())
                .seats(seatInfos)
                .build();
    }
}
