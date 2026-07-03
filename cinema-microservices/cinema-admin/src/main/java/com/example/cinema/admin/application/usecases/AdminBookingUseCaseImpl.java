package com.example.cinema.admin.application.usecases;

import com.example.cinema.admin.application.dto.BookingDetailResponse;
import com.example.cinema.admin.application.dto.BookingResponse;
import com.example.cinema.admin.application.dto.CreateBookingRequest;
import com.example.cinema.admin.application.dto.DashboardStatsDTO;
import com.example.cinema.admin.application.ports.in.AdminBookingUseCase;
import com.example.cinema.admin.domain.entities.*;
import com.example.cinema.admin.domain.repositories.*;
import com.example.cinema.common.events.BaseEvent;
import com.example.cinema.common.events.BookingConfirmedPayload;
import com.example.cinema.common.exception.ClientException;
import com.example.cinema.common.exception.ServerException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.cinema.admin.application.ports.out.NotificationPort;
import com.example.cinema.admin.application.ports.out.CachePort;
import com.example.cinema.admin.application.utils.SecurityUtils;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBookingUseCaseImpl implements AdminBookingUseCase {

    private final BookingRepository bookingRepository;
    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    private final NotificationPort notificationPort;
    private final CachePort cachePort;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BookingDetailResponse> getAllBookings() {
        log.info("Fetching all bookings for admin");
        try {
            List<Booking> bookings = bookingRepository.findAll();
            String staffCinemaId = SecurityUtils.getStaffCinemaId();
            
            if (staffCinemaId != null && !staffCinemaId.trim().isEmpty()) {
                bookings = bookings.stream().filter(booking -> {
                    Showtime showtime = showtimeRepository.findById(booking.getShowtimeId()).orElse(null);
                    if (showtime != null) {
                        Room room = roomRepository.findById(showtime.getRoomId()).orElse(null);
                        return room != null && staffCinemaId.equals(room.getCinemaId());
                    }
                    return false;
                }).collect(Collectors.toList());
            }

            return bookings.stream().map(booking -> {
                String username = "N/A";
                if ("GUEST".equals(booking.getUserId()) || (booking.getPaymentTransactionId() != null && booking.getPaymentTransactionId().startsWith("DIRECT_SALE_"))) {
                    username = "Khách vãn lai";
                } else {
                    User user = userRepository.findById(booking.getUserId()).orElse(null);
                    if (user != null) {
                        username = user.getUsername();
                    }
                }

                String movieTitle = "N/A";
                String roomName = "N/A";
                LocalDateTime showtimeStart = null;
                LocalDateTime showtimeEnd = null;

                Showtime showtime = showtimeRepository.findById(booking.getShowtimeId()).orElse(null);
                if (showtime != null) {
                    showtimeStart = showtime.getStartTime();
                    showtimeEnd = showtime.getEndTime();
                    Movie movie = movieRepository.findById(showtime.getMovieId()).orElse(null);
                    if (movie != null) {
                        movieTitle = movie.getTitle();
                    }
                    Room room = roomRepository.findById(showtime.getRoomId()).orElse(null);
                    if (room != null) {
                        roomName = room.getName();
                    }
                }

                List<BookingDetailResponse.SeatInfo> seats = new ArrayList<>();
                if (booking.getSeats() != null) {
                    for (BookingSeat bs : booking.getSeats()) {
                        String rowLabel = "??";
                        Integer colNumber = 0;
                        String type = "STANDARD";

                        Seat seat = seatRepository.findById(bs.getSeatId()).orElse(null);
                        if (seat != null) {
                            rowLabel = seat.getRowLabel();
                            colNumber = seat.getColNumber();
                            type = seat.getType();
                        }

                        seats.add(BookingDetailResponse.SeatInfo.builder()
                                .seatId(bs.getSeatId())
                                .rowLabel(rowLabel)
                                .colNumber(colNumber)
                                .type(type)
                                .price(bs.getPrice())
                                .build());
                    }
                }

                return BookingDetailResponse.builder()
                        .id(booking.getId())
                        .userId(booking.getUserId())
                        .username(username)
                        .movieTitle(movieTitle)
                        .roomName(roomName)
                        .showtimeStart(showtimeStart)
                        .showtimeEnd(showtimeEnd)
                        .totalPrice(booking.getTotalPrice())
                        .status(booking.getStatus())
                        .checkedIn(booking.isCheckedIn())
                        .expiresAt(booking.getExpiresAt())
                        .paymentTransactionId(booking.getPaymentTransactionId())
                        .createdAt(booking.getCreatedAt())
                        .seats(seats)
                        .build();
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch booking list: {}", e.getMessage(), e);
            throw new ServerException("Failed to fetch booking list: " + e.getMessage(), e);
        }
    }

    @Override
    public BookingResponse createDirectBooking(CreateBookingRequest request, String staffId) {
        log.info("Staff [{}] placing direct booking for Showtime: [{}], Seats: {}", staffId, request.getShowtimeId(),
                request.getSeatIds());

        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new ClientException("Seat list cannot be empty!");
        }

        // --- Bắt đầu Redis Distributed Lock ---
        List<String> lockedKeys = new ArrayList<>();
        boolean isLockedAll = true;

        for (String seatId : request.getSeatIds()) {
            String lockKey = "lock:showtime:" + request.getShowtimeId() + ":seat:" + seatId;
            Boolean success = cachePort.setIfAbsent(lockKey, "LOCKED", Duration.ofSeconds(15));
            if (Boolean.TRUE.equals(success)) {
                lockedKeys.add(lockKey);
            } else {
                isLockedAll = false;
                break;
            }
        }

        if (!isLockedAll) {
            if (!lockedKeys.isEmpty()) {
                cachePort.delete(lockedKeys);
            }
            throw new ClientException("One or more seats you selected are currently being processed by another user. Please try again!");
        }

        try {
            // Gọi helper method có Transaction để đảm bảo Commit xong xuôi mới giải phóng lock
            return executeCreateDirectBooking(request, staffId);
        } finally {
            // --- Giải phóng Redis Lock sau khi Transaction đã COMMIT xong ---
            if (!lockedKeys.isEmpty()) {
                cachePort.delete(lockedKeys);
            }
        }
    }

    @Transactional
    public BookingResponse executeCreateDirectBooking(CreateBookingRequest request, String staffId) {
        // 1. Check if seats are occupied
        if (bookingRepository.isAnySeatOccupied(request.getShowtimeId(), request.getSeatIds())) {
            throw new ClientException("One or more seats are already booked. Please choose other seats!");
        }

        // 2. Fetch showtime and seat details
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ClientException("Showtime not found"));

        List<Seat> seats = new ArrayList<>();
        for (String id : request.getSeatIds()) {
            Seat seat = seatRepository.findById(id)
                    .orElseThrow(() -> new ClientException("Seat not found with ID: " + id));
            seats.add(seat);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<BookingSeat> bookingSeats = new ArrayList<>();

        for (Seat seat : seats) {
            BigDecimal price = calculateSeatPrice(seat.getType(), showtime);
            totalAmount = totalAmount.add(price);

            bookingSeats.add(BookingSeat.builder()
                    .seatId(seat.getId())
                    .showtimeId(request.getShowtimeId())
                    .price(price)
                    .build());
        }

        // 3. Create confirmed booking immediately
        Booking booking = Booking.builder()
                .userId("GUEST")
                .showtimeId(request.getShowtimeId())
                .totalPrice(totalAmount)
                .status("CONFIRMED")
                .paymentTransactionId("DIRECT_SALE_" + System.currentTimeMillis())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .seats(bookingSeats)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        // Publish confirmation event to Kafka
        publishBookingConfirmedEvent(savedBooking);

        BookingResponse response = modelMapper.map(savedBooking, BookingResponse.class);
        response.setSeatIds(request.getSeatIds());
        response.setPaymentUrl("DIRECT");
        return response;
    }

    private BigDecimal calculateSeatPrice(String type, Showtime showtime) {
        BigDecimal basePrice = showtime.getPrice() != null ? showtime.getPrice() : new BigDecimal("75000");

        if (type == null)
            return basePrice;

        return switch (type.toUpperCase()) {
            case "VIP" -> showtime.getPriceVip() != null ? showtime.getPriceVip() : basePrice.multiply(new BigDecimal("1.5"));
            case "COUPLE" -> showtime.getPriceCouple() != null ? showtime.getPriceCouple() : basePrice.multiply(new BigDecimal("2.0"));
            default -> basePrice;
        };
    }

    private void publishBookingConfirmedEvent(Booking booking) {
        try {
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    log.info("Starting async payload preparation for direct booking: [{}]", booking.getId());
                    String email = "customer@cinema.com";
                    if ("GUEST".equals(booking.getUserId())) {
                        email = "guest@cinema.com";
                    } else {
                        User user = userRepository.findById(booking.getUserId()).orElse(null);
                        if (user != null) {
                            email = user.getEmail();
                        }
                    }

                    String movieTitle = "N/A";
                    String roomName = "N/A";
                    Showtime showtime = showtimeRepository.findById(booking.getShowtimeId()).orElse(null);
                    if (showtime != null) {
                        Movie movie = movieRepository.findById(showtime.getMovieId()).orElse(null);
                        if (movie != null) {
                            movieTitle = movie.getTitle();
                        }
                        Room room = roomRepository.findById(showtime.getRoomId()).orElse(null);
                        if (room != null) {
                            roomName = room.getName();
                        }
                    }

                    List<BookingConfirmedPayload.SeatInfo> seatInfos = new java.util.ArrayList<>();
                    if (booking.getSeats() != null) {
                        for (BookingSeat bs : booking.getSeats()) {
                            String seatLabel = "??";
                            Seat seat = seatRepository.findById(bs.getSeatId()).orElse(null);
                            if (seat != null) {
                                seatLabel = seat.getRowLabel() + seat.getColNumber();
                            }
                            seatInfos.add(new BookingConfirmedPayload.SeatInfo(bs.getSeatId(), seatLabel, bs.getPrice()));
                        }
                    }

                    BookingConfirmedPayload payload = BookingConfirmedPayload.builder()
                            .bookingId(booking.getId())
                            .userId(booking.getUserId())
                            .email(email)
                            .movieTitle(movieTitle)
                            .roomName(roomName)
                            .showtimeStart(showtime != null ? showtime.getStartTime() : LocalDateTime.now())
                            .seats(seatInfos)
                            .totalPrice(booking.getTotalPrice())
                            .paymentTransactionId(booking.getPaymentTransactionId())
                            .qrCodeData("https://cinema.example.com/tickets/verify/" + booking.getId())
                            .build();

                    BaseEvent<BookingConfirmedPayload> event = BaseEvent.create("BOOKING_CONFIRMED", payload);
                    log.info("Publishing event BOOKING_CONFIRMED to Kafka topic [booking-events]");
                    notificationPort.sendNotification("booking-events", booking.getId(), event);
                } catch (Exception ex) {
                    log.error("Error in async event publishing for booking [{}]", booking.getId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Failed to submit async event publishing task for booking [{}]", booking.getId(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        log.info("Compiling dashboard statistics...");
        try {
            DashboardStatsDTO stats = new DashboardStatsDTO();
            String staffCinemaId = SecurityUtils.getStaffCinemaId();

            List<User> allUsers = userRepository.findAll();
            List<Movie> allMovies = movieRepository.findAll();
            List<Showtime> allShowtimes = showtimeRepository.findAll();
            List<Room> allRooms = roomRepository.findAll();
            List<Booking> allBookings = bookingRepository.findAll();

            if (staffCinemaId != null && !staffCinemaId.trim().isEmpty()) {
                allRooms = allRooms.stream()
                        .filter(r -> staffCinemaId.equals(r.getCinemaId()))
                        .collect(Collectors.toList());
                        
                List<String> roomIds = allRooms.stream().map(Room::getId).collect(Collectors.toList());
                
                allShowtimes = allShowtimes.stream()
                        .filter(s -> roomIds.contains(s.getRoomId()))
                        .collect(Collectors.toList());
                        
                Set<String> movieIds = allShowtimes.stream().map(Showtime::getMovieId).collect(Collectors.toSet());
                
                allMovies = allMovies.stream()
                        .filter(m -> movieIds.contains(m.getId()))
                        .collect(Collectors.toList());
                        
                Set<String> showtimeIds = allShowtimes.stream().map(Showtime::getId).collect(Collectors.toSet());
                
                allBookings = allBookings.stream()
                        .filter(b -> showtimeIds.contains(b.getShowtimeId()))
                        .collect(Collectors.toList());
            }

            // 1. DB Totals
            stats.setTotalUsers(allUsers.size()); // Users is system-wide normally, but keep as is
            stats.setTotalMovies(allMovies.size());
            stats.setTotalShowtimes(allShowtimes.size());
            stats.setTotalRooms(allRooms.size());

            // 2. Booking Totals
            stats.setTotalBookings(allBookings.size());
            stats.setPendingBookings(allBookings.stream().filter(b -> "PENDING".equalsIgnoreCase(b.getStatus())).count());
            stats.setConfirmedBookings(allBookings.stream().filter(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus())).count());

            // 3. Compute revenue
            BigDecimal totalRevenue = allBookings.stream()
                    .filter(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus()))
                    .map(Booking::getTotalPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.setTotalRevenue(totalRevenue);

            // 4. Movie status totals
            stats.setMoviesShowing(allMovies.stream().filter(m -> "SHOWING".equalsIgnoreCase(m.getStatus())).count());
            stats.setMoviesComingSoon(allMovies.stream().filter(m -> "COMING_SOON".equalsIgnoreCase(m.getStatus())).count());

            // 5. Showtime status totals
            stats.setShowtimesScheduled(allShowtimes.stream().filter(s -> "SCHEDULED".equalsIgnoreCase(s.getStatus())).count());
            stats.setShowtimesPlaying(allShowtimes.stream().filter(s -> "PLAYING".equalsIgnoreCase(s.getStatus())).count());
            stats.setShowtimesCompleted(allShowtimes.stream().filter(s -> "COMPLETED".equalsIgnoreCase(s.getStatus())).count());

            // 6. Top Movies
            Map<String, Long> movieCounts = allBookings.stream()
                    .filter(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus()))
                    .map(b -> showtimeRepository.findById(b.getShowtimeId()).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(Showtime::getMovieId, Collectors.counting()));

            List<DashboardStatsDTO.TopMovieDTO> topMovies = movieCounts.entrySet().stream()
                    .map(entry -> {
                        Movie movie = movieRepository.findById(entry.getKey()).orElse(null);
                        String title = movie != null ? movie.getTitle() : entry.getKey();
                        return DashboardStatsDTO.TopMovieDTO.builder()
                                .movieId(entry.getKey())
                                .movieTitle(title)
                                .bookingCount(entry.getValue())
                                .build();
                    })
                    .sorted(Comparator.comparingLong(DashboardStatsDTO.TopMovieDTO::getBookingCount).reversed())
                    .limit(5)
                    .collect(Collectors.toList());
            stats.setTopMovies(topMovies);

            return stats;
        } catch (Exception e) {
            log.error("Failed to compile dashboard statistics: {}", e.getMessage(), e);
            throw new ServerException("Failed to compile dashboard statistics: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void checkinBooking(String id) {
        log.info("Checking in booking with ID: {}", id);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ClientException("Không tìm thấy thông tin đặt vé với ID: " + id));

        if (!"CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            throw new ClientException("Chỉ những vé có trạng thái ĐÃ THANH TOÁN (CONFIRMED) mới được phép check-in!");
        }

        if (booking.isCheckedIn()) {
            throw new ClientException("Vé này đã được check-in trước đó!");
        }

        booking.updateCheckIn(true);
        bookingRepository.save(booking);
        log.info("Successfully checked in booking with ID: {}", id);
    }
}
