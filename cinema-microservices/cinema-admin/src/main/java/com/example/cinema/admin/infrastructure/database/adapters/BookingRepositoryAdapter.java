package com.example.cinema.admin.infrastructure.database.adapters;

import com.example.cinema.admin.domain.entities.Booking;
import com.example.cinema.admin.domain.entities.BookingSeat;
import com.example.cinema.admin.domain.repositories.BookingRepository;
import com.example.cinema.admin.infrastructure.database.entities.BookingJpaEntity;
import com.example.cinema.admin.infrastructure.database.entities.BookingSeatJpaEntity;
import com.example.cinema.admin.infrastructure.database.repositories.SpringDataBookingRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class BookingRepositoryAdapter implements BookingRepository {

    private final SpringDataBookingRepository springDataBookingRepository;

    public BookingRepositoryAdapter(SpringDataBookingRepository springDataBookingRepository) {
        this.springDataBookingRepository = springDataBookingRepository;
    }

    @Override
    public Booking save(Booking booking) {
        BookingJpaEntity entity = toEntity(booking);
        // Set backreference for seats Jpa
        if (entity.getSeats() != null) {
            for (BookingSeatJpaEntity seatEntity : entity.getSeats()) {
                seatEntity.setBooking(entity);
            }
        }
        BookingJpaEntity saved = springDataBookingRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Booking> findById(String id) {
        return springDataBookingRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Booking> findAll() {
        return springDataBookingRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByStatus(String status) {
        return springDataBookingRepository.findByStatus(status).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAnySeatOccupied(String showtimeId, List<String> seatIds) {
        return springDataBookingRepository.existsByShowtimeIdAndSeatIdInAndStatusIn(showtimeId, seatIds);
    }

    @Override
    public void deleteById(String id) {
        springDataBookingRepository.deleteById(id);
    }

    private Booking toDomain(BookingJpaEntity entity) {
        List<BookingSeat> seats = null;
        if (entity.getSeats() != null) {
            seats = entity.getSeats().stream()
                    .map(s -> BookingSeat.builder()
                            .id(s.getId())
                            .bookingId(entity.getId())
                            .seatId(s.getSeatId())
                            .showtimeId(s.getShowtimeId())
                            .price(s.getPrice())
                            .build())
                    .collect(Collectors.toList());
        }

        return Booking.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .showtimeId(entity.getShowtimeId())
                .totalPrice(entity.getTotalPrice())
                .status(entity.getStatus())
                .checkedIn(entity.isCheckedIn())
                .expiresAt(entity.getExpiresAt())
                .paymentTransactionId(entity.getPaymentTransactionId())
                .createdAt(entity.getCreatedAt())
                .seats(seats)
                .build();
    }

    private BookingJpaEntity toEntity(Booking domain) {
        BookingJpaEntity entity = BookingJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .showtimeId(domain.getShowtimeId())
                .totalPrice(domain.getTotalPrice())
                .status(domain.getStatus())
                .checkedIn(domain.isCheckedIn())
                .expiresAt(domain.getExpiresAt())
                .paymentTransactionId(domain.getPaymentTransactionId())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getSeats() != null) {
            List<BookingSeatJpaEntity> seats = domain.getSeats().stream()
                    .map(s -> BookingSeatJpaEntity.builder()
                            .id(s.getId())
                            .booking(entity)
                            .seatId(s.getSeatId())
                            .showtimeId(s.getShowtimeId())
                            .price(s.getPrice())
                            .build())
                    .collect(Collectors.toList());
            entity.setSeats(seats);
        }
        return entity;
    }
}
