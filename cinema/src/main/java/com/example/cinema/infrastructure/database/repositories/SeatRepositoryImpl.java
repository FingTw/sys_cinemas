package com.example.cinema.infrastructure.database.repositories;

import com.example.cinema.domain.entities.Seat;
import com.example.cinema.domain.repositories.SeatRepository;
import com.example.cinema.infrastructure.database.entities.SeatJpaEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SeatRepositoryImpl implements SeatRepository {
    private final SpringDataSeatRepository repository;

    public SeatRepositoryImpl(SpringDataSeatRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Seat> findAll() {
        return repository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Seat> saveAll(List<Seat> seats) {
        List<SeatJpaEntity> jpaList = seats.stream().map(this::mapToJpa).collect(Collectors.toList());
        List<SeatJpaEntity> savedList = repository.saveAll(jpaList);
        return savedList.stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public Seat save(Seat seat) {
        SeatJpaEntity saved = repository.save(mapToJpa(seat));
        return mapToDomain(saved);
    }

    @Override
    public Optional<Seat> findById(String id) {
        return repository.findById(id).map(this::mapToDomain);
    }

    @Override
    public List<Seat> findByRoomId(String roomId) {
        return repository.findByRoomId(roomId).stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteByRoomId(String roomId) {
        repository.deleteByRoomId(roomId);
    }

    private SeatJpaEntity mapToJpa(Seat seat) {
        return SeatJpaEntity.builder()
                .id(seat.getId())
                .roomId(seat.getRoomId())
                .rowLabel(seat.getRowLabel())
                .colNumber(seat.getColNumber())
                .type(seat.getType())
                .status(seat.getStatus())
                .build();
    }

    private Seat mapToDomain(SeatJpaEntity jpa) {
        return Seat.builder()
                .id(jpa.getId())
                .roomId(jpa.getRoomId())
                .rowLabel(jpa.getRowLabel())
                .colNumber(jpa.getColNumber())
                .type(jpa.getType())
                .status(jpa.getStatus())
                .build();
    }
}
