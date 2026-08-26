package com.example.cinema.booking.application.usecase;

import com.example.cinema.booking.application.dto.*;
import com.example.cinema.booking.application.port.*;
import com.example.cinema.booking.domain.Booking;
import com.example.cinema.common.exception.ClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test cho BookingService — SERVICE LAYER.
 *
 * TIÊU CHUẨN:
 * - @ExtendWith(MockitoExtension.class) → không cần Spring context
 * - Mock toàn bộ I/O: DB, Redis, Feign, Payment
 * - Chỉ test LOGIC của BookingService: validation, orchestration
 * - Không test Mockito hoạt động đúng không — test BUSINESS RULE
 *
 * CẤU TRÚC AAA: Arrange → Act → Assert
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService")
class BookingServiceTest {

    // ─── Mocks (tất cả I/O dependencies) ─────────────────────────
    @Mock private BookingRepositoryPort bookingRepository;
    @Mock private ShowtimeClientPort showtimeClient;
    @Mock private FacilityClientPort facilityClient;
    @Mock private CatalogClientPort catalogClient;
    @Mock private PaymentGatewayPort paymentGatewayPort;
    @Mock private UserClientPort userClient;
    @Mock private BookingEventPublisherPort bookingEventPublisher;
    @Mock private ModelMapper modelMapper;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOps;

    // Class under test — Mockito tự inject @Mock vào đây
    @InjectMocks
    private BookingService bookingService;

    // ─── Test fixtures (data dùng chung) ─────────────────────────
    private ShowtimeDTO validShowtime;
    private SeatDTO seatA1;
    private SeatDTO seatA2;
    private CreateBookingRequest validRequest;

    @BeforeEach
    void setUp() {
        // Showtime hợp lệ: bắt đầu 2 giờ nữa
        validShowtime = new ShowtimeDTO();
        validShowtime.setId("show-001");
        validShowtime.setRoomId("room-001");
        validShowtime.setMovieTitle("Avengers");
        validShowtime.setStartTime(LocalDateTime.now().plusHours(2));
        validShowtime.setPrice(new BigDecimal("75000"));
        validShowtime.setPriceVip(new BigDecimal("120000"));

        seatA1 = new SeatDTO();
        seatA1.setId("seat-A1");
        seatA1.setType("STANDARD");
        seatA1.setRowLabel("A");
        seatA1.setColNumber(1);

        seatA2 = new SeatDTO();
        seatA2.setId("seat-A2");
        seatA2.setType("VIP");
        seatA2.setRowLabel("A");
        seatA2.setColNumber(2);

        validRequest = new CreateBookingRequest();
        validRequest.setShowtimeId("show-001");
        validRequest.setSeatIds(List.of("seat-A1", "seat-A2"));
        validRequest.setPaymentMethod("ONLINE");

        // Redis mock setup (cần cho mọi test tạo booking)
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    // ═══════════════════════════════════════════════════════════
    // createBooking() — Validation cases
    // ═══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("createBooking() - validation")
    class CreateBookingValidation {

        @Test
        @DisplayName("Ném ClientException khi seatIds rỗng")
        void createBooking_emptySeatIds_throwsClientException() {
            // Arrange
            validRequest.setSeatIds(List.of());

            // Act & Assert
            assertThatThrownBy(() -> bookingService.createBooking(validRequest, "user-1", "127.0.0.1"))
                    .isInstanceOf(ClientException.class)
                    .hasMessageContaining("ghe");

            // Verify: không gọi bất kỳ external service nào
            verifyNoInteractions(showtimeClient, facilityClient, bookingRepository);
        }

        @Test
        @DisplayName("Ném ClientException khi seatIds là null")
        void createBooking_nullSeatIds_throwsClientException() {
            validRequest.setSeatIds(null);

            assertThatThrownBy(() -> bookingService.createBooking(validRequest, "user-1", "127.0.0.1"))
                    .isInstanceOf(ClientException.class);
        }

        @Test
        @DisplayName("Ném ClientException khi showtime không tìm thấy")
        void createBooking_showtimeNotFound_throwsClientException() {
            // Arrange: showtime service trả về empty
            when(showtimeClient.getShowtimeById("show-001")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookingService.createBooking(validRequest, "user-1", "127.0.0.1"))
                    .isInstanceOf(ClientException.class)
                    .hasMessageContaining("suat chieu");

            verifyNoInteractions(facilityClient, bookingRepository);
        }

        @Test
        @DisplayName("Ném ClientException khi suất chiếu đã bắt đầu")
        void createBooking_showtimeAlreadyStarted_throwsClientException() {
            // Arrange: showtime bắt đầu 1 giờ TRƯỚC
            validShowtime.setStartTime(LocalDateTime.now().minusHours(1));
            when(showtimeClient.getShowtimeById("show-001")).thenReturn(Optional.of(validShowtime));

            // Act & Assert
            assertThatThrownBy(() -> bookingService.createBooking(validRequest, "user-1", "127.0.0.1"))
                    .isInstanceOf(ClientException.class)
                    .hasMessageContaining("da bat dau");
        }

        @Test
        @DisplayName("Ném ClientException khi ghế không tồn tại trong phòng")
        void createBooking_seatNotInRoom_throwsClientException() {
            // Arrange: phòng chỉ có 1 ghế nhưng request 2 ghế
            when(showtimeClient.getShowtimeById("show-001")).thenReturn(Optional.of(validShowtime));
            when(facilityClient.getSeatsByRoomId("room-001")).thenReturn(List.of(seatA1)); // chỉ có A1

            // Act & Assert
            assertThatThrownBy(() -> bookingService.createBooking(validRequest, "user-1", "127.0.0.1"))
                    .isInstanceOf(ClientException.class)
                    .hasMessageContaining("khong ton tai");
        }

        @Test
        @DisplayName("Ném ClientException khi ghế đang bị người khác giữ (Redis lock thất bại)")
        void createBooking_seatAlreadyLocked_throwsClientException() {
            // Arrange
            when(showtimeClient.getShowtimeById("show-001")).thenReturn(Optional.of(validShowtime));
            when(facilityClient.getSeatsByRoomId("room-001")).thenReturn(List.of(seatA1, seatA2));
            // Redis: ghế A1 lock thành công, A2 đã bị lock bởi người khác → false
            when(valueOps.setIfAbsent(contains("seat-A1"), anyString(), any())).thenReturn(true);
            when(valueOps.setIfAbsent(contains("seat-A2"), anyString(), any())).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> bookingService.createBooking(validRequest, "user-1", "127.0.0.1"))
                    .isInstanceOf(ClientException.class)
                    .hasMessageContaining("dang duoc xu ly");

            // Verify: lock đã bị giải phóng
            verify(redisTemplate).delete(anyCollection());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // getSeatStatusesByShowtime() — Read logic
    // ═══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getSeatStatusesByShowtime()")
    class GetSeatStatuses {

        @Test
        @DisplayName("Trả về ghế AVAILABLE khi chưa có booking nào")
        void getSeatStatuses_noBookings_allSeatsAvailable() {
            // Arrange
            when(showtimeClient.getShowtimeById("show-001")).thenReturn(Optional.of(validShowtime));
            when(facilityClient.getSeatsByRoomId("room-001")).thenReturn(List.of(seatA1, seatA2));
            when(bookingRepository.findOccupiedSeatIdsByShowtime("show-001")).thenReturn(List.of());

            // Act
            var statuses = bookingService.getSeatStatusesByShowtime("show-001");

            // Assert
            assertThat(statuses).hasSize(2);
            assertThat(statuses).allMatch(s -> "AVAILABLE".equals(s.getStatus()));
        }

        @Test
        @DisplayName("Trả về SOLD cho ghế đã có booking CONFIRMED hoặc PENDING")
        void getSeatStatuses_seatOccupied_statusIsSold() {
            // Arrange
            when(showtimeClient.getShowtimeById("show-001")).thenReturn(Optional.of(validShowtime));
            when(facilityClient.getSeatsByRoomId("room-001")).thenReturn(List.of(seatA1, seatA2));
            when(bookingRepository.findOccupiedSeatIdsByShowtime("show-001"))
                    .thenReturn(List.of("seat-A1")); // A1 đã bị chiếm

            // Act
            var statuses = bookingService.getSeatStatusesByShowtime("show-001");

            // Assert
            assertThat(statuses)
                    .filteredOn(s -> "seat-A1".equals(s.getSeatId()))
                    .extracting(SeatStatusDTO::getStatus)
                    .containsOnly("SOLD");
            assertThat(statuses)
                    .filteredOn(s -> "seat-A2".equals(s.getSeatId()))
                    .extracting(SeatStatusDTO::getStatus)
                    .containsOnly("AVAILABLE");
        }

        @Test
        @DisplayName("Tính giá VIP theo priceVip của showtime")
        void getSeatStatuses_vipSeat_usesVipPrice() {
            // Arrange
            when(showtimeClient.getShowtimeById("show-001")).thenReturn(Optional.of(validShowtime));
            when(facilityClient.getSeatsByRoomId("room-001")).thenReturn(List.of(seatA2)); // A2 là VIP
            when(bookingRepository.findOccupiedSeatIdsByShowtime("show-001")).thenReturn(List.of());

            // Act
            var statuses = bookingService.getSeatStatusesByShowtime("show-001");

            // Assert: giá VIP phải là 120.000, không phải 75.000
            assertThat(statuses.get(0).getPrice()).isEqualByComparingTo(new BigDecimal("120000"));
        }

        @Test
        @DisplayName("Ném ClientException khi showtime không tồn tại")
        void getSeatStatuses_showtimeNotFound_throwsClientException() {
            when(showtimeClient.getShowtimeById("show-999")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.getSeatStatusesByShowtime("show-999"))
                    .isInstanceOf(ClientException.class);
        }
    }
}
