package com.example.cinema.infrastructure.database.repositories;

import com.example.cinema.domain.entities.Showtime;
import com.example.cinema.domain.repositories.ShowtimeRepository;
import com.example.cinema.infrastructure.database.entities.ShowtimeJpaEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ShowtimeRepositoryImpl implements ShowtimeRepository {
    private final SpringDataShowtimeRepository repository;

    public ShowtimeRepositoryImpl(SpringDataShowtimeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Showtime save(Showtime showtime) {
        ShowtimeJpaEntity jpa = ShowtimeJpaEntity.builder()
                .id(showtime.getId())
                .movieId(showtime.getMovieId())
                .roomId(showtime.getRoomId())
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                .status(showtime.getStatus())
                .price(showtime.getPrice())
                .priceVip(showtime.getPriceVip())
                .priceCouple(showtime.getPriceCouple())
                .build();
        ShowtimeJpaEntity saved = repository.save(jpa);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Showtime> findById(String id) {
        return repository.findById(id).map(this::mapToDomain);
    }

    @Override
    public List<Showtime> findAll() {
        return repository.findAll().stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    @Override
    public List<Showtime> findConflicts(String roomId, LocalDateTime newStart, LocalDateTime newEnd) {
        return repository.findConflicts(roomId, newStart, newEnd)
                .stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public List<Showtime> findByMovieId(String movieId) {
        return repository.findByMovieId(movieId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Showtime> findScheduledStartedBefore(LocalDateTime now) {
        return repository.findByStatusAndStartTimeLessThanEqual("SCHEDULED", now)
                .stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public List<Showtime> findPlayingEndedBefore(LocalDateTime now) {
        return repository.findByStatusAndEndTimeLessThanEqual("PLAYING", now)
                .stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    private Showtime mapToDomain(ShowtimeJpaEntity jpa) {
        return Showtime.builder()
                .id(jpa.getId())
                .movieId(jpa.getMovieId())
                .roomId(jpa.getRoomId())
                .startTime(jpa.getStartTime())
                .endTime(jpa.getEndTime())
                .status(jpa.getStatus())
                .price(jpa.getPrice() != null ? jpa.getPrice() : new BigDecimal("75000"))
                .priceVip(jpa.getPriceVip() != null ? jpa.getPriceVip() : new BigDecimal("120000"))
                .priceCouple(jpa.getPriceCouple() != null ? jpa.getPriceCouple() : new BigDecimal("195000"))
                .build();
    }
}
