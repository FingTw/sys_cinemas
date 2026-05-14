package com.example.cinema.scheduling.infrastructure.database.adapters;

import com.example.cinema.scheduling.domain.entities.Showtime;
import com.example.cinema.scheduling.domain.repositories.ShowtimeRepository;
import com.example.cinema.scheduling.infrastructure.database.entities.ShowtimeJpaEntity;
import com.example.cinema.scheduling.infrastructure.database.repositories.SpringDataShowtimeRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ShowtimeRepositoryAdapter implements ShowtimeRepository {

    private final SpringDataShowtimeRepository springDataShowtimeRepository;

    public ShowtimeRepositoryAdapter(SpringDataShowtimeRepository springDataShowtimeRepository) {
        this.springDataShowtimeRepository = springDataShowtimeRepository;
    }

    @Override
    public Showtime save(Showtime showtime) {
        ShowtimeJpaEntity entity = toEntity(showtime);
        ShowtimeJpaEntity saved = springDataShowtimeRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Showtime> findById(String id) {
        return springDataShowtimeRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Showtime> findAll() {
        return springDataShowtimeRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        springDataShowtimeRepository.deleteById(id);
    }

    @Override
    public List<Showtime> findConflicts(String roomId, LocalDateTime newStart, LocalDateTime newEnd) {
        return springDataShowtimeRepository.findConflicts(roomId, newStart, newEnd).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Showtime> findByMovieId(String movieId) {
        return springDataShowtimeRepository.findByMovieId(movieId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Showtime> findScheduledStartedBefore(LocalDateTime now) {
        return springDataShowtimeRepository.findByStatusAndStartTimeLessThanEqual("SCHEDULED", now).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Showtime> findPlayingEndedBefore(LocalDateTime now) {
        return springDataShowtimeRepository.findByStatusAndEndTimeLessThanEqual("PLAYING", now).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private Showtime toDomain(ShowtimeJpaEntity entity) {
        return Showtime.builder()
                .id(entity.getId())
                .movieId(entity.getMovieId())
                .roomId(entity.getRoomId())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .status(entity.getStatus())
                .price(entity.getPrice())
                .priceVip(entity.getPriceVip())
                .priceCouple(entity.getPriceCouple())
                .build();
    }

    private ShowtimeJpaEntity toEntity(Showtime domain) {
        return ShowtimeJpaEntity.builder()
                .id(domain.getId())
                .movieId(domain.getMovieId())
                .roomId(domain.getRoomId())
                .startTime(domain.getStartTime())
                .endTime(domain.getEndTime())
                .status(domain.getStatus())
                .price(domain.getPrice())
                .priceVip(domain.getPriceVip())
                .priceCouple(domain.getPriceCouple())
                .build();
    }
}
