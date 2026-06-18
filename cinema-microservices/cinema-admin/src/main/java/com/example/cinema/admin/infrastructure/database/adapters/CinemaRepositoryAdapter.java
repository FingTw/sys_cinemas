package com.example.cinema.admin.infrastructure.database.adapters;

import com.example.cinema.admin.domain.entities.Cinema;
import com.example.cinema.admin.domain.repositories.CinemaRepository;
import com.example.cinema.admin.infrastructure.database.entities.CinemaJpaEntity;
import com.example.cinema.admin.infrastructure.database.repositories.SpringDataCinemaRepository;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CinemaRepositoryAdapter implements CinemaRepository {

    private final SpringDataCinemaRepository springDataCinemaRepository;

    @Override
    public Cinema save(Cinema cinema) {
        CinemaJpaEntity entity = toEntity(cinema);
        CinemaJpaEntity saved = springDataCinemaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Cinema> findById(String id) {
        return springDataCinemaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Cinema> findAll() {
        return springDataCinemaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Cinema> findByComplexId(String complexId) {
        return springDataCinemaRepository.findByComplexId(complexId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        springDataCinemaRepository.deleteById(id);
    }

    private Cinema toDomain(CinemaJpaEntity entity) {
        return Cinema.builder()
                .id(entity.getId())
                .name(entity.getName())
                .address(entity.getAddress())
                .complexId(entity.getComplexId())
                .build();
    }

    private CinemaJpaEntity toEntity(Cinema domain) {
        return CinemaJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .address(domain.getAddress())
                .complexId(domain.getComplexId())
                .build();
    }
}
