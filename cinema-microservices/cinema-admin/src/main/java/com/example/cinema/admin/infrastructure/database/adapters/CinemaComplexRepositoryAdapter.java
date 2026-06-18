package com.example.cinema.admin.infrastructure.database.adapters;

import com.example.cinema.admin.domain.entities.CinemaComplex;
import com.example.cinema.admin.domain.repositories.CinemaComplexRepository;
import com.example.cinema.admin.infrastructure.database.entities.CinemaComplexJpaEntity;
import com.example.cinema.admin.infrastructure.database.repositories.SpringDataCinemaComplexRepository;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CinemaComplexRepositoryAdapter implements CinemaComplexRepository {

    private final SpringDataCinemaComplexRepository springDataCinemaComplexRepository;

    @Override
    public CinemaComplex save(CinemaComplex complex) {
        CinemaComplexJpaEntity entity = toEntity(complex);
        CinemaComplexJpaEntity saved = springDataCinemaComplexRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<CinemaComplex> findById(String id) {
        return springDataCinemaComplexRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<CinemaComplex> findAll() {
        return springDataCinemaComplexRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        springDataCinemaComplexRepository.deleteById(id);
    }

    private CinemaComplex toDomain(CinemaComplexJpaEntity entity) {
        return CinemaComplex.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }

    private CinemaComplexJpaEntity toEntity(CinemaComplex domain) {
        return CinemaComplexJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .build();
    }
}
