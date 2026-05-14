package com.example.cinema.infrastructure.database.repositories;

import com.example.cinema.domain.entities.Booking;
import com.example.cinema.domain.entities.BookingSeat;
import com.example.cinema.domain.repositories.BookingRepository;
import com.example.cinema.infrastructure.database.entities.BookingJpaEntity;
import com.example.cinema.infrastructure.database.entities.BookingSeatJpaEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class BookingRepositoryImpl implements BookingRepository {

    private final SpringDataBookingRepository springDataRepository;

    public BookingRepositoryImpl(SpringDataBookingRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Booking save(Booking booking) {
        BookingJpaEntity jpa = mapToJpa(booking);
        // Map seats back for saving if they exist
        if (booking.getSeats() != null) {
            jpa.setSeats(booking.getSeats().stream()
                    .map(s -> BookingSeatJpaEntity.builder()
                            .id(s.getId())
                            .booking(jpa)
                            .seatId(s.getSeatId())
                            .showtimeId(s.getShowtimeId())
                            .price(s.getPrice())
                            .build())
                    .collect(Collectors.toList()));
        }
        
        BookingJpaEntity saved = springDataRepository.save(jpa);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Booking> findById(String id) {
        return springDataRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public List<Booking> findByUserId(String userId) {
        return springDataRepository.findByUserId(userId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public List<Booking> findExpiredPendingBookings() {
        return springDataRepository.findExpiredPendingBookings(LocalDateTime.now()).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAnySeatOccupied(String showtimeId, List<String> seatIds) {
        return springDataRepository.existsByShowtimeIdAndSeatIdInAndStatusIn(showtimeId, seatIds);
    }

    @Override
    public List<Booking> findAll() {
        return springDataRepository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByStatus(String status) {
        return springDataRepository.countByStatus(status);
    }

    @Override
    public long countAll() {
        return springDataRepository.count();
    }

    private Booking mapToDomain(BookingJpaEntity jpa) {
        return Booking.builder()
                .id(jpa.getId())
                .userId(jpa.getUserId())
                .showtimeId(jpa.getShowtimeId())
                .totalPrice(jpa.getTotalPrice())
                .status(jpa.getStatus())
                .expiresAt(jpa.getExpiresAt())
                .paymentTransactionId(jpa.getPaymentTransactionId())
                .createdAt(jpa.getCreatedAt())
                .seats(jpa.getSeats() != null ? jpa.getSeats().stream()
                        .map(s -> BookingSeat.builder()
                                .id(s.getId())
                                .bookingId(jpa.getId())
                                .seatId(s.getSeatId())
                                .showtimeId(s.getShowtimeId())
                                .price(s.getPrice())
                                .build())
                        .collect(Collectors.toList()) : null)
                .build();
    }

    private BookingJpaEntity mapToJpa(Booking booking) {
        return BookingJpaEntity.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .showtimeId(booking.getShowtimeId())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .expiresAt(booking.getExpiresAt())
                .paymentTransactionId(booking.getPaymentTransactionId())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
