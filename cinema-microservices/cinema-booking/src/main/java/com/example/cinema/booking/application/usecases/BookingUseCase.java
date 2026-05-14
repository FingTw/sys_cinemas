package com.example.cinema.booking.application.usecases;

import com.example.cinema.booking.application.dto.BookingResponse;
import com.example.cinema.booking.application.dto.CreateBookingRequest;
import com.example.cinema.booking.application.dto.SeatStatusDTO;
import com.example.cinema.booking.domain.entities.Booking;
import com.example.cinema.booking.domain.entities.BookingSeat;
import com.example.cinema.booking.infrastructure.feign.ShowtimeClient;
import com.example.cinema.booking.infrastructure.feign.FacilityClient;
import com.example.cinema.booking.application.dto.feign.ShowtimeDTO;
import com.example.cinema.booking.application.dto.feign.SeatDTO;
import com.example.cinema.booking.domain.repositories.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.cinema.common.exception.ClientException;
import com.example.cinema.common.exception.ServerException;
import com.example.cinema.booking.application.ports.out.PaymentGatewayPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingUseCase {

    private static final Logger log = LoggerFactory.getLogger(BookingUseCase.class);

    private final BookingRepository bookingRepository;
    private final FacilityClient facilityClient;
    private final ShowtimeClient showtimeClient;
    private final PaymentGatewayPort paymentGatewayPort;

    public BookingUseCase(BookingRepository bookingRepository, FacilityClient facilityClient, ShowtimeClient showtimeClient, PaymentGatewayPort paymentGatewayPort) {
        this.bookingRepository = bookingRepository;
        this.facilityClient = facilityClient;
        this.showtimeClient = showtimeClient;
        this.paymentGatewayPort = paymentGatewayPort;
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, String userId, String ipAddress) {
        log.info("Dang xu ly dat ve cho User: [{}], Showtime: [{}], Seats: {}", userId, request.getShowtimeId(),
                request.getSeatIds());

        try {
            // 0. Idempotency: Kiem tra user da co booking PENDING/CONFIRMED cho suat chieu nay chua
            List<Booking> existingBookings = bookingRepository.findByUserId(userId);
            boolean hasDuplicate = existingBookings.stream()
                    .anyMatch(b -> request.getShowtimeId().equals(b.getShowtimeId())
                            && ("PENDING".equals(b.getStatus()) || "CONFIRMED".equals(b.getStatus())));
            if (hasDuplicate) {
                throw new ClientException("Ban da co don dat ve dang hoat dong cho suat chieu nay. Vui long kiem tra lai lich su dat ve.");
            }

            // 1. Kiem tra ghe da bi ai dat chua (PENDING hoac CONFIRMED)
            // Day la lop bao ve BE: Chan concurrency o tang Application
            if (bookingRepository.isAnySeatOccupied(request.getShowtimeId(), request.getSeatIds())) {
                throw new ClientException(
                        "Mot hoac nhieu ghe ban chon vua moi duoc nguoi khac giu cho. Vui long chon ghe khac!");
            }

            // 2. Lay thong tin ghe va suat chieu de tinh tien
            ShowtimeDTO showtime;
            try {
                showtime = showtimeClient.getShowtimeById(request.getShowtimeId()).orElseThrow(() -> new ClientException("Suat chieu khong ton tai"));
            } catch (Exception e) {
                throw new ClientException("Khong tim thay suat chieu hoac loi ket noi den Scheduling Service");
            }

            List<SeatDTO> seats = new ArrayList<>();
            for (String id : request.getSeatIds()) {
                try {
                    seats.add(facilityClient.getSeatById(id).orElseThrow(() -> new ClientException("Ghe khong ton tai")));
                } catch (Exception e) {
                    throw new ClientException("Khong tim thay ghe ID: " + id);
                }
            }

            BigDecimal totalAmount = BigDecimal.ZERO;
            List<BookingSeat> bookingSeats = new ArrayList<>();

            for (SeatDTO seat : seats) {
                BigDecimal price = calculateSeatPrice(seat.getType(), showtime);
                totalAmount = totalAmount.add(price);

                bookingSeats.add(BookingSeat.builder()
                        .seatId(seat.getId())
                        .showtimeId(request.getShowtimeId())
                        .price(price)
                        .build());
            }

            // 3. Tao Booking voi trang thai PENDING va het han sau 5 phut
            Booking booking = Booking.builder()
                    .userId(userId)
                    .showtimeId(request.getShowtimeId())
                    .totalPrice(totalAmount)
                    .status("PENDING")
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .seats(bookingSeats)
                    .build();

            Booking savedBooking = bookingRepository.save(booking);

            // 4. Tao URL thanh toan VNPay
            String paymentUrl = paymentGatewayPort.createPaymentUrl(savedBooking.getId(), totalAmount.longValue(), ipAddress);

            return BookingResponse.builder()
                    .id(savedBooking.getId())
                    .userId(savedBooking.getUserId())
                    .showtimeId(savedBooking.getShowtimeId())
                    .totalPrice(savedBooking.getTotalPrice())
                    .status(savedBooking.getStatus())
                    .expiresAt(savedBooking.getExpiresAt())
                    .seatIds(request.getSeatIds())
                    .paymentUrl(paymentUrl)
                    .build();
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Loi he thong khi xu ly dat ve: {}", e.getMessage(), e);
            throw new ServerException("Loi he thong khi tao don dat ve: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo đơn đặt vé trực tiếp tại quầy (Staff/Admin).
     * Bỏ qua cổng thanh toán Online, xác nhận CONFIRMED ngay lập tức.
     */
    @Transactional
    public BookingResponse createDirectBooking(CreateBookingRequest request, String userId) {
        log.info("Staff [{}] dang tao don dat ve truc tiep cho Showtime: [{}], Seats: {}", userId, request.getShowtimeId(),
                request.getSeatIds());

        try {
            // 1. Kiem tra ghe da bi ai dat chua
            if (bookingRepository.isAnySeatOccupied(request.getShowtimeId(), request.getSeatIds())) {
                throw new ClientException("Mot hoac nhieu ghe da duoc dat. Vui long chon ghe khac!");
            }

            // 2. Lay thong tin suat chieu va ghe
            ShowtimeDTO showtime;
            try {
                showtime = showtimeClient.getShowtimeById(request.getShowtimeId()).orElseThrow(() -> new ClientException("Suat chieu khong ton tai"));
            } catch (Exception e) {
                throw new ClientException("Khong tim thay suat chieu hoac loi ket noi den Scheduling Service");
            }

            List<SeatDTO> seats = new ArrayList<>();
            for (String id : request.getSeatIds()) {
                try {
                    seats.add(facilityClient.getSeatById(id).orElseThrow(() -> new ClientException("Ghe khong ton tai")));
                } catch (Exception e) {
                    throw new ClientException("Khong tim thay ghe ID: " + id);
                }
            }

            BigDecimal totalAmount = BigDecimal.ZERO;
            List<BookingSeat> bookingSeats = new ArrayList<>();

            for (SeatDTO seat : seats) {
                BigDecimal price = calculateSeatPrice(seat.getType(), showtime);
                totalAmount = totalAmount.add(price);

                bookingSeats.add(BookingSeat.builder()
                        .seatId(seat.getId())
                        .showtimeId(request.getShowtimeId())
                        .price(price)
                        .build());
            }

            // 3. Tao Booking voi trang thai CONFIRMED ngay lap tuc
            Booking booking = Booking.builder()
                    .userId(userId)
                    .showtimeId(request.getShowtimeId())
                    .totalPrice(totalAmount)
                    .status("CONFIRMED")
                    .paymentTransactionId("DIRECT_SALE_" + System.currentTimeMillis())
                    .expiresAt(LocalDateTime.now().plusDays(1)) // Khong het han ngay vi da thanh toan
                    .seats(bookingSeats)
                    .build();

            Booking savedBooking = bookingRepository.save(booking);

            return BookingResponse.builder()
                    .id(savedBooking.getId())
                    .userId(savedBooking.getUserId())
                    .showtimeId(savedBooking.getShowtimeId())
                    .totalPrice(savedBooking.getTotalPrice())
                    .status(savedBooking.getStatus())
                    .expiresAt(savedBooking.getExpiresAt())
                    .seatIds(request.getSeatIds())
                    .paymentUrl("DIRECT") // Danh dau la thanh toan truc tiep
                    .build();
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Loi khi tao don ban truc tiep: {}", e.getMessage(), e);
            throw new ServerException("Loi he thong: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void confirmPayment(String bookingId, String transactionId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ClientException("Khong tim thay don dat ve"));

            if ("CONFIRMED".equals(booking.getStatus())) {
                return;
            }

            booking.setStatus("CONFIRMED");
            booking.setPaymentTransactionId(transactionId);
            bookingRepository.save(booking);
            log.info("Xac nhan thanh toan thanh cong cho Booking ID: [{}]", bookingId);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Loi CSDL khi xac nhan thanh toan cho Booking ID [{}]: {}", bookingId, e.getMessage(), e);
            throw new ServerException("Loi he thong khi xac nhan thanh toan: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<SeatStatusDTO> getSeatStatusesByShowtime(String showtimeId) {
        try {
            // 1. Lay thong tin suat chieu de biet phong chieu (roomId)
            ShowtimeDTO showtime;
            try {
                showtime = showtimeClient.getShowtimeById(showtimeId).orElseThrow(() -> new ClientException("Suat chieu khong ton tai"));
            } catch (Exception e) {
                throw new ClientException("Khong tim thay suat chieu ID: " + showtimeId);
            }

            // 2. Lay tat ca ghe cua phong chieu tuong ung
            List<SeatDTO> allSeats;
            try {
                allSeats = facilityClient.getSeatsByRoomId(showtime.getRoomId());
            } catch (Exception e) {
                throw new ClientException("Loi khi lay thong tin ghe tu Facility Service");
            }

            return allSeats.stream().map(seat -> {
                String status = "AVAILABLE";

                // Kiem tra xem ghe nay co nam trong bat ky booking nao dang PENDING/CONFIRMED
                // cua suat chieu nay khong
                if (bookingRepository.isAnySeatOccupied(showtimeId, List.of(seat.getId()))) {
                    // Phan biet HELD (dang cho) va SOLD (da mua)
                    // (Can bo sung method chi tiet hon trong repository de biet status cu the)
                    status = "SOLD";
                }

                return SeatStatusDTO.builder()
                        .seatId(seat.getId())
                        .rowLabel(seat.getRowLabel())
                        .colNumber(seat.getColNumber())
                        .type(seat.getType())
                        .status(status)
                        .price(calculateSeatPrice(seat.getType(), showtime))
                        .build();
            }).collect(Collectors.toList());
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Loi he thong khi lay trang thai ghe cua suat chieu [{}]: {}", showtimeId, e.getMessage(), e);
            throw new ServerException("Loi he thong khi lay thong tin ghe: " + e.getMessage(), e);
        }
    }

    private BigDecimal calculateSeatPrice(String type, ShowtimeDTO showtime) {
        BigDecimal basePrice = showtime.getPrice() != null ? showtime.getPrice() : new BigDecimal("75000");

        if (type == null)
            return basePrice;

        return switch (type.toUpperCase()) {
            case "VIP" -> showtime.getPriceVip() != null ? showtime.getPriceVip() : basePrice.multiply(new BigDecimal("1.5"));
            case "COUPLE" -> showtime.getPriceCouple() != null ? showtime.getPriceCouple() : basePrice.multiply(new BigDecimal("2.0"));
            default -> basePrice; // STANDARD
        };
    }
}
