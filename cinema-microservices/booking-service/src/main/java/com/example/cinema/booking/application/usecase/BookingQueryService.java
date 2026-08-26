package com.example.cinema.booking.application.usecase;

import com.example.cinema.booking.application.dto.BookingDetailResponse;
import com.example.cinema.booking.application.dto.SeatDTO;
import com.example.cinema.booking.application.dto.ShowtimeDTO;
import com.example.cinema.booking.domain.Booking;
import com.example.cinema.booking.application.port.BookingRepositoryPort;
import com.example.cinema.booking.application.port.ShowtimeClientPort;
import com.example.cinema.booking.application.port.FacilityClientPort;
import com.example.cinema.booking.application.port.CatalogClientPort;
import com.example.cinema.booking.application.port.UserClientPort;
import com.example.cinema.booking.application.port.PaymentGatewayPort;
import com.example.cinema.booking.application.port.BookingEventPublisherPort;
import com.example.cinema.booking.application.dto.UserDTO;
import com.example.cinema.common.exception.ClientException;
import com.example.cinema.common.exception.ServerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.modelmapper.ModelMapper;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.cinema.booking.adapter.feign.clients.UserClient;
import com.example.cinema.booking.adapter.feign.clients.FacilityClient;
import com.example.cinema.booking.adapter.feign.clients.ShowtimeClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingQueryService {

    private final BookingRepositoryPort bookingRepository;
    private final ShowtimeClientPort showtimeClient;
    private final FacilityClientPort facilityClient;
    private final UserClientPort userClient;
    private final ModelMapper modelMapper;

        @Transactional(readOnly = true)
    public List<BookingDetailResponse> getMyBookings(String userId) {
        log.info("Truy van lich su dat ve cho User: [{}]", userId);
        try {
            String username = null;
            try {
                username = userClient.getUserById(userId).map(UserDTO::getUsername).orElse(null);
            } catch (Exception e) {
                log.warn("Loi khi goi UserClient de lay username cua user [{}]: {}", userId, e.getMessage());
            }
            final String finalUsername = username;
            
            List<Booking> bookings = bookingRepository.findByUserId(userId);
            return bookings.stream()
                    .map(b -> enrichBooking(b, finalUsername))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Loi khi truy van lich su dat ve cua User [{}]: {}", userId, e.getMessage(), e);
            throw new ServerException("Loi he thong khi truy van lich su dat ve: " + e.getMessage(), e);
        }
    }

        @Transactional(readOnly = true)
    public BookingDetailResponse getBookingDetail(String bookingId, String userId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ClientException("Khong tim thay don dat ve ID: " + bookingId));

            if (!booking.getUserId().equals(userId)) {
                throw new ClientException("Ban khong co quyen xem don dat ve nay.");
            }

            String username = null;
            try {
                username = userClient.getUserById(userId).map(UserDTO::getUsername).orElse(null);
            } catch (Exception e) {
                log.warn("Loi khi goi UserClient de lay username cua user [{}]: {}", userId, e.getMessage());
            }

            return enrichBooking(booking, username);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Loi khi truy van chi tiet Booking [{}]: {}", bookingId, e.getMessage(), e);
            throw new ServerException("Loi he thong khi truy van chi tiet don dat ve: " + e.getMessage(), e);
        }
    }

        @Transactional
    public void cancelBooking(String bookingId, String userId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ClientException("Khong tim thay don dat ve ID: " + bookingId));

            // 1. Thực hiện check điều kiện và logic trên Entity
            booking.cancelByUser(userId);
            
            // 2. Lưu trạng thái CANCELLED (thay vì xóa vật lý)
            bookingRepository.save(booking);
            log.info("Da huy Booking ID: [{}]", bookingId);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Loi khi huy Booking [{}]: {}", bookingId, e.getMessage(), e);
            throw new ServerException("Loi he thong khi huy don dat ve: " + e.getMessage(), e);
        }
    }

        @Transactional(readOnly = true)
    public BookingDetailResponse getBookingDetailInternal(String bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ClientException("Khong tim thay don dat ve ID: " + bookingId));

            String username = null;
            if (booking.getUserId() != null) {
                try {
                    username = userClient.getUserById(booking.getUserId()).map(UserDTO::getUsername).orElse(null);
                } catch (Exception e) {
                    log.warn("Loi khi goi UserClient de lay username cua user [{}]: {}", booking.getUserId(), e.getMessage());
                }
            }


            return enrichBooking(booking, username);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Loi khi truy van chi tiet Booking (internal) [{}]: {}", bookingId, e.getMessage(), e);
            throw new ServerException("Loi he thong khi truy van chi tiet don dat ve: " + e.getMessage(), e);
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
                .showtimeId(booking.getShowtimeId())
                .movieId(showtime != null ? showtime.getMovieId() : null)
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
