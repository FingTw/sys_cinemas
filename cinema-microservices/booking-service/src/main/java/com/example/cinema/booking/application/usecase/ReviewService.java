package com.example.cinema.booking.application.usecase;

import com.example.cinema.booking.application.dto.CreateReviewRequest;
import com.example.cinema.booking.application.dto.ReviewResponse;
import com.example.cinema.booking.application.dto.ShowtimeDTO;
import com.example.cinema.booking.application.dto.UserDTO;
import com.example.cinema.booking.application.port.BookingRepositoryPort;
import com.example.cinema.booking.application.port.ReviewRepositoryPort;
import com.example.cinema.booking.application.port.ShowtimeClientPort;
import com.example.cinema.booking.application.port.UserClientPort;
import com.example.cinema.booking.domain.Booking;
import com.example.cinema.booking.domain.Review;
import com.example.cinema.common.exception.ClientException;
import com.example.cinema.common.exception.ServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepositoryPort reviewRepository;
    private final BookingRepositoryPort bookingRepository;
    private final ShowtimeClientPort showtimeClient;
    private final UserClientPort userClient;

    /**
     * Kiểm tra xem user có thể review booking này không.
     * Điều kiện: booking CONFIRMED + showtimeEnd đã qua.
     */
    @Transactional(readOnly = true)
    public boolean canReview(String bookingId, String userId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null || !booking.getUserId().equals(userId)) return false;
        if (!"CONFIRMED".equals(booking.getStatus())) return false;
        if (reviewRepository.existsByBookingId(bookingId)) return false;

        ShowtimeDTO showtime = showtimeClient.getShowtimeById(booking.getShowtimeId()).orElse(null);
        if (showtime == null || showtime.getEndTime() == null) return false;

        return LocalDateTime.now().isAfter(showtime.getEndTime());
    }

    /**
     * Tạo review mới cho một booking đã xem phim.
     */
    @Transactional
    public ReviewResponse createReview(String bookingId, String userId, CreateReviewRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ClientException("Không tìm thấy đơn đặt vé ID: " + bookingId));

        if (!booking.getUserId().equals(userId)) {
            throw new ClientException("Bạn không có quyền đánh giá đơn đặt vé này.");
        }
        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new ClientException("Chỉ có thể đánh giá sau khi thanh toán thành công.");
        }
        if (reviewRepository.existsByBookingId(bookingId)) {
            throw new ClientException("Bạn đã đánh giá bộ phim này rồi.");
        }

        ShowtimeDTO showtime = showtimeClient.getShowtimeById(booking.getShowtimeId())
                .orElseThrow(() -> new ServerException("Không thể lấy thông tin suất chiếu."));

        if (showtime.getEndTime() == null || LocalDateTime.now().isBefore(showtime.getEndTime())) {
            throw new ClientException("Chỉ có thể đánh giá sau khi buổi chiếu kết thúc.");
        }

        String movieId = showtime.getMovieId();
        Review review = Review.create(bookingId, userId, movieId, request.getRating(), request.getComment());
        Review saved = reviewRepository.save(review);

        log.info("User [{}] đã đánh giá phim [{}] qua booking [{}], rating={}",
                userId, movieId, bookingId, request.getRating());

        String username = null;
        try {
            username = userClient.getUserById(userId).map(UserDTO::getUsername).orElse(null);
        } catch (Exception e) {
            log.warn("Không thể lấy username của user [{}]: {}", userId, e.getMessage());
        }

        return toResponse(saved, username, showtime.getMovieTitle());
    }

    /**
     * Lấy danh sách reviews của một bộ phim (public).
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByMovie(String movieId) {
        return reviewRepository.findByMovieId(movieId).stream()
                .map(r -> {
                    String username = null;
                    try {
                        username = userClient.getUserById(r.getUserId())
                                .map(UserDTO::getUsername).orElse(null);
                    } catch (Exception e) {
                        log.warn("Không thể lấy username cho user [{}]", r.getUserId());
                    }
                    return toResponse(r, username, null);
                })
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách reviews của user hiện tại.
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getMyReviews(String userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(r -> toResponse(r, null, null))
                .collect(Collectors.toList());
    }

    /**
     * Lấy review theo bookingId (để check đã review chưa).
     */
    @Transactional(readOnly = true)
    public ReviewResponse getReviewByBooking(String bookingId, String userId) {
        return reviewRepository.findByBookingId(bookingId)
                .filter(r -> r.getUserId().equals(userId))
                .map(r -> toResponse(r, null, null))
                .orElse(null);
    }

    private ReviewResponse toResponse(Review r, String username, String movieTitle) {
        return ReviewResponse.builder()
                .id(r.getId())
                .bookingId(r.getBookingId())
                .userId(r.getUserId())
                .username(username)
                .movieId(r.getMovieId())
                .movieTitle(movieTitle)
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
