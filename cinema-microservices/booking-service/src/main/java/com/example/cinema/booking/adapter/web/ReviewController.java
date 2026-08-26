package com.example.cinema.booking.adapter.web;

import com.example.cinema.booking.application.dto.CreateReviewRequest;
import com.example.cinema.booking.application.dto.ReviewResponse;
import com.example.cinema.booking.application.usecase.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Kiểm tra xem user có thể review booking này không.
     */
    @PreAuthorize("hasAuthority('BOOKING_READ')")
    @GetMapping("/can-review/{bookingId}")
    public ResponseEntity<Map<String, Object>> canReview(
            @PathVariable String bookingId,
            Authentication authentication) {
        String userId = extractUserId(authentication);
        boolean canReview = reviewService.canReview(bookingId, userId);
        ReviewResponse existing = reviewService.getReviewByBooking(bookingId, userId);
        return ResponseEntity.ok(Map.of(
                "canReview", canReview,
                "reviewed", existing != null,
                "review", existing != null ? existing : Map.of()
        ));
    }

    /**
     * Tạo review mới.
     */
    @PreAuthorize("hasAuthority('BOOKING_READ')")
    @PostMapping("/{bookingId}")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable String bookingId,
            @RequestBody @Valid CreateReviewRequest request,
            Authentication authentication) {
        String userId = extractUserId(authentication);
        return ResponseEntity.ok(reviewService.createReview(bookingId, userId, request));
    }

    /**
     * Lấy danh sách reviews của một phim (public, không cần auth).
     */
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ReviewResponse>> getMovieReviews(@PathVariable String movieId) {
        return ResponseEntity.ok(reviewService.getReviewsByMovie(movieId));
    }

    /**
     * Lấy danh sách reviews của user hiện tại.
     */
    @PreAuthorize("hasAuthority('BOOKING_READ')")
    @GetMapping("/my")
    public ResponseEntity<List<ReviewResponse>> getMyReviews(Authentication authentication) {
        String userId = extractUserId(authentication);
        return ResponseEntity.ok(reviewService.getMyReviews(userId));
    }

    private String extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Không thể xác thực người dùng.");
        }
        return authentication.getName();
    }
}
