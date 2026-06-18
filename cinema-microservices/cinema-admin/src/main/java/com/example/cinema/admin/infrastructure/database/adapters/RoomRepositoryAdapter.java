package com.example.cinema.admin.infrastructure.database.adapters;

import com.example.cinema.admin.domain.entities.Room;
import com.example.cinema.admin.domain.repositories.RoomRepository;
import com.example.cinema.admin.infrastructure.database.entities.RoomJpaEntity;
import com.example.cinema.admin.infrastructure.database.repositories.SpringDataRoomRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class RoomRepositoryAdapter implements RoomRepository {

    private final SpringDataRoomRepository springDataRoomRepository;

    public RoomRepositoryAdapter(SpringDataRoomRepository springDataRoomRepository) {
        this.springDataRoomRepository = springDataRoomRepository;
    }

    @Override
    public Room save(Room room) {
        RoomJpaEntity entity = toEntity(room);
        RoomJpaEntity saved = springDataRoomRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Room> findById(String id) {
        return springDataRoomRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Room> findAll() {
        return springDataRoomRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        springDataRoomRepository.deleteById(id);
    }

    private Room toDomain(RoomJpaEntity entity) {
        return Room.builder()
                .id(entity.getId())
                .name(entity.getName())
                .status(entity.getStatus())
                .gridRows(entity.getGridRows())
                .gridCols(entity.getGridCols())
                .cinemaId(entity.getCinemaId())
                .build();
    }

    private RoomJpaEntity toEntity(Room domain) {
        return RoomJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .status(domain.getStatus())
                .gridRows(domain.getGridRows())
                .gridCols(domain.getGridCols())
                .cinemaId(domain.getCinemaId())
                .build();
    }
}
