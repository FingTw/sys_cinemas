package com.example.cinema.booking.application.usecases;

import com.example.cinema.booking.application.dto.BookingResponse;
import com.example.cinema.booking.application.dto.CreateBookingRequest;
import com.example.cinema.booking.application.dto.SeatStatusDTO;
import com.example.cinema.booking.domain.entities.Booking;
import com.example.cinema.booking.domain.entities.BookingSeat;
import com.example.cinema.booking.domain.entities.BookingItem;
import com.example.cinema.booking.application.ports.out.ShowtimePort;
import com.example.cinema.booking.application.ports.out.FacilityPort;
import com.example.cinema.booking.application.ports.out.CatalogPort;
import com.example.cinema.booking.application.ports.out.UserPort;
import com.example.cinema.booking.application.ports.out.EventPublisherPort;
import com.example.cinema.booking.application.dto.feign.ShowtimeDTO;
import com.example.cinema.booking.application.dto.feign.SeatDTO;
import com.example.cinema.booking.application.dto.feign.UserDTO;
import com.example.cinema.common.events.BookingConfirmedPayload;
import com.example.cinema.booking.domain.repositories.BookingRepository;
import com.example.cinema.common.exception.ClientException;
import com.example.cinema.common.exception.ServerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.cinema.booking.application.ports.in.BookingService;
import com.example.cinema.booking.application.ports.out.PaymentGatewayPort;
import org.modelmapper.ModelMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ShowtimePort showtimePort;
    private final FacilityPort facilityPort;
    private final CatalogPort catalogPort;
    private final PaymentGatewayPort paymentGatewayPort;
    private final UserPort userPort;
    private final EventPublisherPort eventPublisherPort;
    private final ModelMapper modelMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public BookingResponse createBooking(CreateBookingRequest request, String userId, String ipAddress) {
        log.info("Dang xu ly dat ve cho User: [{}], Showtime: [{}], Seats: {}", userId, request.getShowtimeId(),
                request.getSeatIds());

        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new ClientException("Danh sach ghe khong duoc de trong!");
        }

        // --- Bắt đầu Redis Distributed Lock ---
        List<String> lockedKeys = new ArrayList<>();
        boolean isLockedAll = true;

        for (String seatId : request.getSeatIds()) {
            String lockKey = "lock:showtime:" + request.getShowtimeId() + ":seat:" + seatId;
            Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", Duration.ofSeconds(15));
            if (Boolean.TRUE.equals(success)) {
                lockedKeys.add(lockKey);
            } else {
                isLockedAll = false;
                break;
            }
        }

        if (!isLockedAll) {
            if (!lockedKeys.isEmpty()) {
                redisTemplate.delete(lockedKeys);
            }
            throw new ClientException("Mot hoac nhieu ghe ban chon dang duoc xu ly boi nguoi khac. Vui long chon ghe khac!");
        }

        try {
            // Gọi helper method có Transaction để đảm bảo Commit xong xuôi mới giải phóng lock
            return executeCreateBooking(request, userId, ipAddress);
        } finally {
            // --- Giải phóng Redis Lock sau khi Transaction đã COMMIT xong ---
            if (!lockedKeys.isEmpty()) {
                redisTemplate.delete(lockedKeys);
            }
        }
    }

    @Transactional
    public BookingResponse executeCreateBooking(CreateBookingRequest request, String userId, String ipAddress) {
        // 0. Idempotency check removed as requested by user. 
        // User can now book the same showtime multiple times.

        // 1. Kiem tra ghe da bi ai dat chua (PENDING hoac CONFIRMED)
        if (bookingRepository.isAnySeatOccupied(request.getShowtimeId(), request.getSeatIds())) {
            throw new ClientException(
                    "Mot hoac nhieu ghe ban chon vua moi duoc nguoi khac giu cho. Vui long chon ghe khac!");
        }

        // 2. Lay thong tin ghe va suat chieu de tinh tien
        ShowtimeDTO showtime;
        try {
            showtime = showtimePort.getShowtimeById(request.getShowtimeId()).orElseThrow(() -> new ClientException("Suat chieu khong ton tai"));
        } catch (Exception e) {
            throw new ClientException("Khong tim thay suat chieu hoac loi ket noi den Scheduling Service");
        }

        if (showtime.getStartTime() != null && showtime.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ClientException("Khong the dat ve cho suat chieu da bat dau hoac trong qua khu!");
        }

        List<SeatDTO> seats = new ArrayList<>();
        for (String id : request.getSeatIds()) {
            try {
                seats.add(facilityPort.getSeatById(id).orElseThrow(() -> new ClientException("Ghe khong ton tai")));
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
        Booking booking = Booking.create(userId, request.getShowtimeId(), bookingSeats, new ArrayList<>(), 5);

        Booking savedBooking = bookingRepository.save(booking);

        // 4. Tao URL thanh toan VNPay
        String paymentUrl = paymentGatewayPort.createPaymentUrl(savedBooking.getId(), totalAmount.longValue(), ipAddress);

        BookingResponse response = modelMapper.map(savedBooking, BookingResponse.class);
        response.setSeatIds(request.getSeatIds());
        response.setPaymentUrl(paymentUrl);
        return response;
    }


    @Override
    @Transactional
    public void confirmPayment(String bookingId, String transactionId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ClientException("Khong tim thay don dat ve"));

            if ("CONFIRMED".equals(booking.getStatus())) {
                return;
            }

            booking.confirmPayment(transactionId);
            Booking savedBooking = bookingRepository.save(booking);
            log.info("Xac nhan thanh toan thanh cong cho Booking ID: [{}]", bookingId);

            // Phat su kien sang Kafka khi thanh toan online thanh cong
            publishBookingConfirmedEvent(savedBooking);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Loi CSDL khi xac nhan thanh toan cho Booking ID [{}]: {}", bookingId, e.getMessage(), e);
            throw new ServerException("Loi he thong khi xac nhan thanh toan: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatStatusDTO> getSeatStatusesByShowtime(String showtimeId) {
        try {
            // 1. Lay thong tin suat chieu de biet phong chieu (roomId)
            ShowtimeDTO showtime;
            try {
                showtime = showtimePort.getShowtimeById(showtimeId).orElseThrow(() -> new ClientException("Suat chieu khong ton tai"));
            } catch (Exception e) {
                throw new ClientException("Khong tim thay suat chieu ID: " + showtimeId);
            }

            // 2. Lay tat ca ghe cua phong chieu tuong ung
            List<SeatDTO> allSeats;
            try {
                allSeats = facilityPort.getSeatsByRoomId(showtime.getRoomId());
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

    private void publishBookingConfirmedEvent(Booking booking) {
        try {
            // Asynchronously build the event payload and publish
            CompletableFuture.runAsync(() -> {
                try {
                    log.info("Starting async payload preparation for booking: [{}]", booking.getId());
                    // 1. Fetch user email
                    String email = "customer@cinema.com"; // fallback
                    try {
                        Optional<UserDTO> userOpt = userPort.getUserById(booking.getUserId());
                        if (userOpt.isPresent()) {
                            email = userOpt.get().getEmail();
                        }
                    } catch (Exception e) {
                        log.warn("Failed to fetch user email for booking [{}], using fallback. Error: {}", booking.getId(), e.getMessage());
                    }

                    // 2. Fetch showtime and movie
                    String movieTitle = "N/A";
                    String roomName = "N/A";
                    try {
                        Optional<ShowtimeDTO> showtimeOpt = showtimePort.getShowtimeById(booking.getShowtimeId());
                        if (showtimeOpt.isPresent()) {
                            movieTitle = showtimeOpt.get().getMovieTitle();
                            roomName = showtimeOpt.get().getRoomName();
                        }
                    } catch (Exception e) {
                        log.warn("Failed to fetch showtime/movie details for booking [{}]. Error: {}", booking.getId(), e.getMessage());
                    }

                    // 3. Fetch seat labels
                    List<BookingConfirmedPayload.SeatInfo> seatInfos = new ArrayList<>();
                    if (booking.getSeats() != null) {
                        for (BookingSeat bs : booking.getSeats()) {
                            String seatLabel = "??";
                            try {
                                Optional<SeatDTO> seatOpt = facilityPort.getSeatById(bs.getSeatId());
                                if (seatOpt.isPresent()) {
                                    seatLabel = seatOpt.get().getRowLabel() + seatOpt.get().getColNumber();
                                }
                            } catch (Exception e) {
                                log.warn("Failed to fetch seat details for seat ID [{}]. Error: {}", bs.getSeatId(), e.getMessage());
                            }
                            seatInfos.add(new BookingConfirmedPayload.SeatInfo(bs.getSeatId(), seatLabel, bs.getPrice()));
                        }
                    }

                    eventPublisherPort.publishBookingConfirmed(booking, seatInfos, movieTitle, roomName, email);
                } catch (Exception ex) {
                    log.error("Error in async event publishing for booking [{}]", booking.getId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Failed to submit async event publishing task for booking [{}]", booking.getId(), e);
        }
    }

    @Override
    public BookingResponse createPendingBooking(String showtimeId, List<String> seatIds, List<com.example.cinema.booking.application.dto.BookingItemRequest> items, String userId) {
        log.info("Camunda: Creating pending booking for User: [{}], Showtime: [{}], Seats: {}", userId, showtimeId, seatIds);

        if (seatIds == null || seatIds.isEmpty()) {
            throw new ClientException("Danh sach ghe khong duoc de trong!");
        }

        List<String> lockedKeys = new ArrayList<>();
        boolean isLockedAll = true;

        for (String seatId : seatIds) {
            String lockKey = "lock:showtime:" + showtimeId + ":seat:" + seatId;
            Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", Duration.ofSeconds(15));
            if (Boolean.TRUE.equals(success)) {
                lockedKeys.add(lockKey);
            } else {
                isLockedAll = false;
                break;
            }
        }

        if (!isLockedAll) {
            if (!lockedKeys.isEmpty()) {
                redisTemplate.delete(lockedKeys);
            }
            throw new ClientException("Mot hoac nhieu ghe ban chon dang duoc xu ly boi nguoi khac. Vui long chon ghe khac!");
        }

        try {
            return executeCreatePendingBooking(showtimeId, seatIds, items, userId);
        } finally {
            if (!lockedKeys.isEmpty()) {
                redisTemplate.delete(lockedKeys);
            }
        }
    }

    @Transactional
    public BookingResponse executeCreatePendingBooking(String showtimeId, List<String> seatIds, List<com.example.cinema.booking.application.dto.BookingItemRequest> itemRequests, String userId) {
        if (bookingRepository.isAnySeatOccupied(showtimeId, seatIds)) {
            throw new ClientException("Mot hoac nhieu ghe ban chon vua moi duoc nguoi khac giu cho. Vui long chon ghe khac!");
        }

        ShowtimeDTO showtime;
        try {
            showtime = showtimePort.getShowtimeById(showtimeId).orElseThrow(() -> new ClientException("Suat chieu khong ton tai"));
        } catch (Exception e) {
            throw new ClientException("Khong tim thay suat chieu hoac loi ket noi den Scheduling Service");
        }

        if (showtime.getStartTime() != null && showtime.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ClientException("Khong the dat ve cho suat chieu da bat dau hoac trong qua khu!");
        }

        List<SeatDTO> seats = new ArrayList<>();
        for (String id : seatIds) {
            try {
                seats.add(facilityPort.getSeatById(id).orElseThrow(() -> new ClientException("Ghe khong ton tai")));
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
                    .showtimeId(showtimeId)
                    .price(price)
                    .build());
        }

        List<BookingItem> bookingItems = new ArrayList<>();
        if (itemRequests != null && !itemRequests.isEmpty()) {
            for (com.example.cinema.booking.application.dto.BookingItemRequest req : itemRequests) {
                com.example.cinema.booking.application.dto.feign.ProductDTO product = catalogPort.getProductById(req.getProductId())
                        .orElseThrow(() -> new ClientException("Không tìm thấy sản phẩm F&B ID: " + req.getProductId()));
                
                bookingItems.add(BookingItem.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .quantity(req.getQuantity())
                        .unitPrice(product.getPrice())
                        .build());
            }
        }

        Booking booking = Booking.create(userId, showtimeId, bookingSeats, bookingItems, 5);

        Booking savedBooking = bookingRepository.save(booking);

        BookingResponse response = modelMapper.map(savedBooking, BookingResponse.class);
        response.setSeatIds(seatIds);
        return response;
    }

    @Override
    @Transactional
    public void confirmBookingStatus(String bookingId, String transactionId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ClientException("Khong tim thay don dat ve"));

            if ("CONFIRMED".equals(booking.getStatus())) {
                return;
            }

            booking.confirmPayment(transactionId);
            bookingRepository.save(booking);
            log.info("Camunda: Xac nhan thanh toan cho Booking ID: [{}]", bookingId);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Loi CSDL khi xac nhan thanh toan cho Booking ID [{}]: {}", bookingId, e.getMessage(), e);
            throw new ServerException("Loi he thong khi xac nhan thanh toan: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void cancelPendingBooking(String bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ClientException("Khong tim thay don dat ve"));

            if ("EXPIRED".equals(booking.getStatus()) || "CANCELLED".equals(booking.getStatus())) {
                return;
            }

            booking.markAsExpired();
            if (booking.getSeats() != null) {
                booking.getSeats().clear();
            }
            bookingRepository.save(booking);
            log.info("Camunda: Huy don hang thanh cong do qua han. ID: [{}]", bookingId);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Loi khi huy don hang [{}]: {}", bookingId, e.getMessage(), e);
            throw new ServerException("Loi he thong khi huy don hang: " + e.getMessage(), e);
        }
    }

    @Override
    public void publishBookingConfirmedEvent(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ClientException("Khong tim thay don dat ve de phat su kien"));
        publishBookingConfirmedEvent(booking);
    }

    @Override
    @Transactional
    public void refundBooking(String bookingId) {
        log.info("Executing refundBooking for Booking ID: [{}]", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ClientException("Không tìm thấy đơn đặt vé để thực hiện hoàn tiền"));

        // 1. Thực hiện nghiệp vụ trên Domain Entity
        booking.refund();

        long amount = booking.getTotalPrice().longValue();
        String transactionId = booking.getPaymentTransactionId();
        
        boolean refundSuccess = paymentGatewayPort.refund(bookingId, amount, transactionId, "127.0.0.1");
        if (!refundSuccess) {
            throw new ServerException("Lỗi kết nối cổng thanh toán khi thực hiện hoàn tiền");
        }

        if (booking.getSeats() != null) {
            booking.getSeats().clear();
        }
        bookingRepository.save(booking);
        log.info("Hoàn tiền thành công cho đơn vé [{}]. Trạng thái đổi sang CANCELLED và giải phóng ghế.", bookingId);
    }
}
