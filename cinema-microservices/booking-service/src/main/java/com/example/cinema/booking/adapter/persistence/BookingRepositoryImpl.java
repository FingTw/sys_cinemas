package com.example.cinema.booking.adapter.persistence;

import com.example.cinema.booking.adapter.persistence.entity.BookingJpaEntity;
import com.example.cinema.booking.application.port.BookingRepositoryPort;
import com.example.cinema.booking.domain.Booking;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class BookingRepositoryImpl implements BookingRepositoryPort {

    private final SpringDataBookingRepository jpaRepository;
    private final BookingMapper mapper;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BookingRepositoryImpl.class);

    @Override
    public Optional<Booking> findById(String id) {
        log.info(">>>> BookingRepositoryImpl.findById CALLED WITH id = [{}]", id);
        Optional<BookingJpaEntity> entityOpt = jpaRepository.findBookingById(id);
        log.info(">>>> jpaRepository.findBookingById({}) returned: {}", id, entityOpt.isPresent() ? "PRESENT" : "EMPTY");
        return entityOpt.map(mapper::toDomain);
    }

    @Override
    public Booking save(Booking booking) {
        BookingJpaEntity jpaEntity = mapper.toJpa(booking);
        BookingJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public List<Booking> findExpiredPendingBookings(LocalDateTime now) {
        return jpaRepository.findExpiredPendingBookings(now).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByUserIdOrderByCreatedAtDesc(String userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAnySeatOccupied(String showtimeId, List<String> seatIds) {
        return jpaRepository.isAnySeatOccupied(showtimeId, seatIds);
    }

    @Override
    public List<String> findOccupiedSeatIdsByShowtime(String showtimeId) {
        return jpaRepository.findOccupiedSeatIdsByShowtime(showtimeId);
    }

    @Override
    public long countByStatus(String status) {
        return jpaRepository.countByStatus(status);
    }
}
