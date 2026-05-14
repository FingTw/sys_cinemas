package com.example.cinema.infrastructure.database.repositories;

import com.example.cinema.domain.entities.Room;
import com.example.cinema.domain.repositories.RoomRepository;
import com.example.cinema.infrastructure.database.entities.RoomJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class RoomRepositoryImpl implements RoomRepository {
    private final SpringDataRoomRepository repository;

    public RoomRepositoryImpl(SpringDataRoomRepository repository) {
        this.repository = repository;
    }

    @Override
    public Room save(Room room) {
        RoomJpaEntity jpa = RoomJpaEntity.builder()
                .id(room.getId())
                .name(room.getName())
                .status(room.getStatus())
                .gridRows(room.getGridRows() != null ? room.getGridRows() : 10)
                .gridCols(room.getGridCols() != null ? room.getGridCols() : 15)
                .build();
        RoomJpaEntity saved = repository.save(jpa);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Room> findById(String id) {
        return repository.findById(id).map(this::mapToDomain);
    }

    @Override
    public List<Room> findAll() {
        return repository.findAll().stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    private Room mapToDomain(RoomJpaEntity jpa) {
        return Room.builder()
                .id(jpa.getId())
                .name(jpa.getName())
                .status(jpa.getStatus())
                .gridRows(jpa.getGridRows() != null ? jpa.getGridRows() : 10)
                .gridCols(jpa.getGridCols() != null ? jpa.getGridCols() : 15)
                .build();
    }
}
