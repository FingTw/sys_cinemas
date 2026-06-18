package com.example.cinema.catalog.infrastructure.database.adapters;

import com.example.cinema.catalog.domain.entities.Genre;
import com.example.cinema.catalog.domain.repositories.GenreRepository;
import com.example.cinema.catalog.infrastructure.database.entities.GenreJpaEntity;
import com.example.cinema.catalog.infrastructure.database.repositories.SpringDataGenreRepository;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GenreRepositoryAdapter implements GenreRepository {

    private final SpringDataGenreRepository springDataGenreRepository;

    @Override
    public Optional<Genre> findById(String id) {
        return springDataGenreRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Genre> findAll() {
        return springDataGenreRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private Genre toDomain(GenreJpaEntity entity) {
        return Genre.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .build();
    }
}
