package com.example.cinema.booking.infrastructure.database.adapters;

import com.example.cinema.booking.domain.entities.Booking;
import com.example.cinema.booking.domain.entities.BookingSeat;
import com.example.cinema.booking.domain.repositories.BookingRepository;
import com.example.cinema.booking.infrastructure.database.entities.BookingJpaEntity;
import com.example.cinema.booking.infrastructure.database.entities.BookingSeatJpaEntity;
import com.example.cinema.booking.infrastructure.database.repositories.SpringDataBookingRepository;
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
        BookingJpaEntity entity;
        if (booking.getId() != null) {
            Optional<BookingJpaEntity> existingOpt = springDataBookingRepository.findById(booking.getId());
            if (existingOpt.isPresent()) {
                entity = existingOpt.get();
                // Update scalar fields only. (Usually status and paymentTransactionId change after creation)
                entity.setStatus(booking.getStatus());
                entity.setPaymentTransactionId(booking.getPaymentTransactionId());
                // We do not replace seats and items here to avoid Hibernate orphan removal exception.
            } else {
                entity = toEntity(booking);
                if (entity.getSeats() != null) {
                    entity.getSeats().forEach(s -> s.setBooking(entity));
                }
            }
        } else {
            entity = toEntity(booking);
            if (entity.getSeats() != null) {
                entity.getSeats().forEach(s -> s.setBooking(entity));
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
    public List<Booking> findByUserId(String userId) {
        return springDataBookingRepository.findByUserId(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByShowtimeId(String showtimeId) {
        // SpringDataBookingRepository chua co findByShowtimeId truc tiep, nhung ta co the implement
        // Hoac lay tat ca roi filter. Tuy nhien, thong thuong ta nen them vao SpringData interface.
        // De nhanh, ta su dung findAll roi filter (tam thoi) hoac check lai SpringData.
        return springDataBookingRepository.findAll().stream()
                .filter(b -> b.getShowtimeId().equals(showtimeId))
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByStatus(String status) {
        return springDataBookingRepository.findAll().stream()
                .filter(b -> b.getStatus().equals(status))
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

    @Override
    public long countAll() {
        return springDataBookingRepository.count();
    }

    @Override
    public long countByStatus(String status) {
        return springDataBookingRepository.countByStatus(status);
    }

    private Booking toDomain(BookingJpaEntity entity) {
        List<BookingSeat> domainSeats = null;
        if (entity.getSeats() != null) {
            domainSeats = entity.getSeats().stream()
                    .map(s -> new BookingSeat(s.getId(), s.getBooking().getId(), s.getSeatId(), s.getShowtimeId(), s.getPrice()))
                    .collect(Collectors.toList());
        }

        return Booking.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .showtimeId(entity.getShowtimeId())
                .totalPrice(entity.getTotalPrice())
                .status(entity.getStatus())
                .expiresAt(entity.getExpiresAt())
                .paymentTransactionId(entity.getPaymentTransactionId())
                .createdAt(entity.getCreatedAt())
                .seats(domainSeats)
                .build();
    }

    private BookingJpaEntity toEntity(Booking domain) {
        List<BookingSeatJpaEntity> entitySeats = null;
        if (domain.getSeats() != null) {
            entitySeats = domain.getSeats().stream()
                    .map(s -> BookingSeatJpaEntity.builder()
                            .id(s.getId())
                            .seatId(s.getSeatId())
                            .showtimeId(s.getShowtimeId())
                            .price(s.getPrice())
                            .build())
                    .collect(Collectors.toList());
        }

        return BookingJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .showtimeId(domain.getShowtimeId())
                .totalPrice(domain.getTotalPrice())
                .status(domain.getStatus())
                .expiresAt(domain.getExpiresAt())
                .paymentTransactionId(domain.getPaymentTransactionId())
                .createdAt(domain.getCreatedAt())
                .seats(entitySeats)
                .build();
    }
}
