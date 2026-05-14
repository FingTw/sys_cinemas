package com.example.cinema.application.usecases;

import com.example.cinema.application.dto.BookingDetailResponse;
import com.example.cinema.application.exceptions.ClientException;
import com.example.cinema.application.exceptions.ServerException;
import com.example.cinema.application.ports.in.BookingQueryUseCase;
import com.example.cinema.domain.entities.*;
import com.example.cinema.domain.repositories.*;
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
    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    public BookingQueryService(BookingRepository bookingRepository,
                               ShowtimeRepository showtimeRepository,
                               MovieRepository movieRepository,
                               RoomRepository roomRepository,
                               SeatRepository seatRepository,
                               UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
        this.seatRepository = seatRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDetailResponse> getMyBookings(String userId) {
        log.info("Truy van lich su dat ve cho User: [{}]", userId);
        try {
            List<Booking> bookings = bookingRepository.findByUserId(userId);
            log.info("Tim thay {} don dat ve cua User [{}]", bookings.size(), userId);
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
        log.info("Truy van chi tiet Booking [{}] cho User [{}]", bookingId, userId);
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ClientException("Khong tim thay don dat ve ID: " + bookingId));

            // Kiểm tra quyền sở hữu: User chỉ được xem booking của mình
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
        log.info("User [{}] yeu cau huy Booking [{}]", userId, bookingId);
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ClientException("Khong tim thay don dat ve ID: " + bookingId));

            // Kiểm tra quyền sở hữu
            if (!booking.getUserId().equals(userId)) {
                throw new ClientException("Ban khong co quyen huy don dat ve nay.");
            }

            // Chỉ cho phép hủy booking PENDING (chưa thanh toán)
            if (!"PENDING".equals(booking.getStatus())) {
                throw new ClientException("Chi co the huy don dat ve dang cho thanh toan (PENDING). Trang thai hien tai: " + booking.getStatus());
            }

            // Xóa hẳn booking để nhả ràng buộc UNIQUE trên booking_seats (giống logic cleanup scheduler)
            bookingRepository.deleteById(bookingId);
            log.info("Da huy va nha ghe cho Booking ID: [{}]", bookingId);
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
        log.info("Admin: Truy van toan bo don dat ve...");
        try {
            List<Booking> bookings = bookingRepository.findAll();
            log.info("Tim thay tong cong {} don dat ve.", bookings.size());
            return bookings.stream()
                    .map(b -> {
                        String username = userRepository.findById(b.getUserId())
                                .map(User::getUsername).orElse("N/A");
                        return enrichBooking(b, username);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Loi khi truy van toan bo booking: {}", e.getMessage(), e);
            throw new ServerException("Loi he thong khi truy van danh sach don dat ve: " + e.getMessage(), e);
        }
    }

    /**
     * Gắn thêm thông tin phim, phòng, suất chiếu, ghế vào booking response.
     */
    private BookingDetailResponse enrichBooking(Booking booking, String usernameOverride) {
        // Lấy thông tin suất chiếu
        Showtime showtime = booking.getShowtimeId() != null
                ? showtimeRepository.findById(booking.getShowtimeId()).orElse(null)
                : null;

        // Lấy thông tin phim
        Movie movie = (showtime != null && showtime.getMovieId() != null)
                ? movieRepository.findById(showtime.getMovieId()).orElse(null)
                : null;

        // Lấy thông tin phòng
        Room room = (showtime != null && showtime.getRoomId() != null)
                ? roomRepository.findById(showtime.getRoomId()).orElse(null)
                : null;

        // Map seat info
        List<BookingDetailResponse.SeatInfo> seatInfos = Collections.emptyList();
        if (booking.getSeats() != null) {
            seatInfos = booking.getSeats().stream().map(bs -> {
                Seat seat = seatRepository.findById(bs.getSeatId()).orElse(null);
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
                .movieTitle(movie != null ? movie.getTitle() : "N/A")
                .roomName(room != null ? room.getName() : "N/A")
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
