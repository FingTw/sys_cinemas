package com.example.cinema.booking.application.usecase;

import com.example.cinema.booking.application.dto.BookingResponse;
import com.example.cinema.booking.application.dto.CreateBookingRequest;
import com.example.cinema.booking.application.dto.SeatStatusDTO;
import com.example.cinema.booking.domain.Booking;
import com.example.cinema.booking.domain.BookingSeat;
import com.example.cinema.booking.domain.BookingItem;
import com.example.cinema.booking.application.dto.ShowtimeDTO;
import com.example.cinema.booking.application.dto.SeatDTO;
import com.example.cinema.booking.application.dto.UserDTO;
import com.example.cinema.common.events.BookingConfirmedPayload;
import com.example.cinema.booking.application.port.BookingRepositoryPort;
import com.example.cinema.booking.application.port.ShowtimeClientPort;
import com.example.cinema.booking.application.port.FacilityClientPort;
import com.example.cinema.booking.application.port.CatalogClientPort;
import com.example.cinema.booking.application.port.UserClientPort;
import com.example.cinema.booking.application.port.PaymentGatewayPort;
import com.example.cinema.booking.application.port.BookingEventPublisherPort;
import com.example.cinema.common.exception.ClientException;
import com.example.cinema.common.exception.ServerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
import com.example.cinema.booking.application.port.BookingEventPublisherPort;
import com.example.cinema.booking.adapter.feign.clients.UserClient;
import com.example.cinema.booking.application.port.PaymentGatewayPort;
import com.example.cinema.booking.adapter.feign.clients.CatalogClient;
import com.example.cinema.booking.adapter.feign.clients.FacilityClient;
import com.example.cinema.booking.adapter.feign.clients.ShowtimeClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepositoryPort bookingRepository;
    private final ShowtimeClientPort showtimeClient;
    private final FacilityClientPort facilityClient;
    private final CatalogClientPort catalogClient;
    private final PaymentGatewayPort paymentGatewayPort;
    private final UserClientPort userClient;
    private final BookingEventPublisherPort bookingEventPublisher;
    private final ModelMapper modelMapper;
    private final StringRedisTemplate redisTemplate;

        public BookingResponse createBooking(CreateBookingRequest request, String userId, String ipAddress) {
        log.info("Dang xu ly dat ve cho User: [{}], Showtime: [{}], Seats: {}", userId, request.getShowtimeId(),
                request.getSeatIds());

        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new ClientException("Danh sach ghe khong duoc de trong!");
        }

        // --- Lấy dữ liệu qua Feign Client TRƯỚC khi mở Transaction và Redis Lock ---
        ShowtimeDTO showtime;
        try {
            showtime = showtimeClient.getShowtimeById(request.getShowtimeId()).orElseThrow(() -> new ClientException("Suat chieu khong ton tai"));
        } catch (Exception e) {
            throw new ClientException("Khong tim thay suat chieu hoac loi ket noi den Scheduling Service");
        }

        if (showtime.getStartTime() != null && showtime.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ClientException("Khong the dat ve cho suat chieu da bat dau hoac trong qua khu!");
        }

        List<SeatDTO> allSeats;
        try {
            allSeats = facilityClient.getSeatsByRoomId(showtime.getRoomId());
        } catch (Exception e) {
            throw new ClientException("Loi khi lay thong tin ghe tu Facility Service");
        }

        List<SeatDTO> seats = allSeats.stream()
                .filter(s -> request.getSeatIds().contains(s.getId()))
                .collect(Collectors.toList());

        if (seats.size() != request.getSeatIds().size()) {
            throw new ClientException("Mot hoac nhieu ghe khong ton tai trong phong chieu nay");
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

        // Delegate sang method @Transactional — lock sẽ được giải phóng bên trong
        // sau khi transaction COMMIT/ROLLBACK hoàn toàn qua afterCompletion hook.
        return executeCreateBooking(request, userId, ipAddress, lockedKeys, showtime, seats);
    }

    @Transactional
    public BookingResponse executeCreateBooking(CreateBookingRequest request, String userId, String ipAddress,
                                                List<String> lockedKeys, ShowtimeDTO showtime, List<SeatDTO> seats) {
        // Đăng ký hook giải phóng Redis lock SAU KHI transaction COMMIT hoặc ROLLBACK xong.
        // afterCompletion đảm bảo lock luôn được xóa dù thành công hay lỗi,
        // và chỉ chạy khi Spring đã hoàn tất commit — không bao giờ giải phóng trước.
        if (!lockedKeys.isEmpty()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    log.debug("[Lock] Giải phóng {} Redis lock sau transaction (status={}).", lockedKeys.size(), status);
                    redisTemplate.delete(lockedKeys);
                }
            });
        }
        // 0. Idempotency check removed as requested by user. 
        // User can now book the same showtime multiple times.

        // 1. Kiem tra ghe da bi ai dat chua (PENDING hoac CONFIRMED)
        if (bookingRepository.isAnySeatOccupied(request.getShowtimeId(), request.getSeatIds())) {
            throw new ClientException(
                    "Mot hoac nhieu ghe ban chon vua moi duoc nguoi khac giu cho. Vui long chon ghe khac!");
        }

        // 2. Tinh toan tien
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

            // 3. Lay danh sach id cac ghe da bi chiem hoac dang pending
            List<String> occupiedSeatIds = bookingRepository.findOccupiedSeatIdsByShowtime(showtimeId);

            return allSeats.stream().map(seat -> {
                String status = "AVAILABLE";

                // Kiem tra xem ghe nay co nam trong bat ky booking nao dang PENDING/CONFIRMED khong
                if (occupiedSeatIds.contains(seat.getId())) {
                    // Phan biet HELD (dang cho) va SOLD (da mua)
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
                        Optional<UserDTO> userOpt = userClient.getUserById(booking.getUserId());
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
                        Optional<ShowtimeDTO> showtimeOpt = showtimeClient.getShowtimeById(booking.getShowtimeId());
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
                                Optional<SeatDTO> seatOpt = facilityClient.getSeatById(bs.getSeatId());
                                if (seatOpt.isPresent()) {
                                    seatLabel = seatOpt.get().getRowLabel() + seatOpt.get().getColNumber();
                                }
                            } catch (Exception e) {
                                log.warn("Failed to fetch seat details for seat ID [{}]. Error: {}", bs.getSeatId(), e.getMessage());
                            }
                            seatInfos.add(new BookingConfirmedPayload.SeatInfo(bs.getSeatId(), seatLabel, bs.getPrice()));
                        }
                    }

                    bookingEventPublisher.publishBookingConfirmed(booking, seatInfos, movieTitle, roomName, email);
                } catch (Exception ex) {
                    log.error("Error in async event publishing for booking [{}]", booking.getId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Failed to submit async event publishing task for booking [{}]", booking.getId(), e);
        }
    }

        public BookingResponse createPendingBooking(String showtimeId, List<String> seatIds, List<com.example.cinema.booking.application.dto.BookingItemRequest> items, String userId) {
        log.info("Camunda: Creating pending booking for User: [{}], Showtime: [{}], Seats: {}", userId, showtimeId, seatIds);

        if (seatIds == null || seatIds.isEmpty()) {
            throw new ClientException("Danh sach ghe khong duoc de trong!");
        }

        // --- Lấy dữ liệu qua Feign Client TRƯỚC khi mở Transaction và Redis Lock ---
        ShowtimeDTO showtime;
        try {
            showtime = showtimeClient.getShowtimeById(showtimeId).orElseThrow(() -> new ClientException("Suat chieu khong ton tai"));
        } catch (Exception e) {
            throw new ClientException("Khong tim thay suat chieu hoac loi ket noi den Scheduling Service");
        }

        if (showtime.getStartTime() != null && showtime.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ClientException("Khong the dat ve cho suat chieu da bat dau hoac trong qua khu!");
        }

        List<SeatDTO> allSeats;
        try {
            allSeats = facilityClient.getSeatsByRoomId(showtime.getRoomId());
        } catch (Exception e) {
            throw new ClientException("Loi khi lay thong tin ghe tu Facility Service");
        }

        List<SeatDTO> seats = allSeats.stream()
                .filter(s -> seatIds.contains(s.getId()))
                .collect(Collectors.toList());

        if (seats.size() != seatIds.size()) {
            throw new ClientException("Mot hoac nhieu ghe khong ton tai trong phong chieu nay");
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
            return executeCreatePendingBooking(showtimeId, seatIds, items, userId, showtime, seats);
        } finally {
            if (!lockedKeys.isEmpty()) {
                redisTemplate.delete(lockedKeys);
            }
        }
    }

    @Transactional
    public BookingResponse executeCreatePendingBooking(String showtimeId, List<String> seatIds, List<com.example.cinema.booking.application.dto.BookingItemRequest> itemRequests, String userId, ShowtimeDTO showtime, List<SeatDTO> seats) {
        if (bookingRepository.isAnySeatOccupied(showtimeId, seatIds)) {
            throw new ClientException("Mot hoac nhieu ghe ban chon vua moi duoc nguoi khac giu cho. Vui long chon ghe khac!");
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
                com.example.cinema.booking.application.dto.ProductDTO product = catalogClient.getProductById(req.getProductId())
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

        public void publishBookingConfirmedEvent(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ClientException("Khong tim thay don dat ve de phat su kien"));
        publishBookingConfirmedEvent(booking);
    }

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
