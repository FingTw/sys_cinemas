package com.example.cinema.facility.infrastructure.database.adapters;

import com.example.cinema.facility.domain.entities.Seat;
import com.example.cinema.facility.domain.repositories.SeatRepository;
import com.example.cinema.facility.infrastructure.database.entities.SeatJpaEntity;
import com.example.cinema.facility.infrastructure.database.repositories.SpringDataSeatRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SeatRepositoryAdapter implements SeatRepository {

    private final SpringDataSeatRepository springDataSeatRepository;

    public SeatRepositoryAdapter(SpringDataSeatRepository springDataSeatRepository) {
        this.springDataSeatRepository = springDataSeatRepository;
    }

    @Override
    public List<Seat> saveAll(List<Seat> seats) {
        List<SeatJpaEntity> entities = seats.stream().map(this::toEntity).collect(Collectors.toList());
        return springDataSeatRepository.saveAll(entities).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Seat save(Seat seat) {
        SeatJpaEntity entity = toEntity(seat);
        SeatJpaEntity saved = springDataSeatRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Seat> findById(String id) {
        return springDataSeatRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Seat> findAll() {
        return springDataSeatRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Seat> findByRoomId(String roomId) {
        return springDataSeatRepository.findByRoomId(roomId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByRoomId(String roomId) {
        springDataSeatRepository.deleteByRoomId(roomId);
    }

    private Seat toDomain(SeatJpaEntity entity) {
        return Seat.builder()
                .id(entity.getId())
                .roomId(entity.getRoomId())
                .rowLabel(entity.getRowLabel())
                .colNumber(entity.getColNumber())
                .type(entity.getType())
                .status(entity.getStatus())
                .build();
    }

    private SeatJpaEntity toEntity(Seat domain) {
        return SeatJpaEntity.builder()
                .id(domain.getId())
                .roomId(domain.getRoomId())
                .rowLabel(domain.getRowLabel())
                .colNumber(domain.getColNumber())
                .type(domain.getType())
                .status(domain.getStatus())
                .build();
    }
}
