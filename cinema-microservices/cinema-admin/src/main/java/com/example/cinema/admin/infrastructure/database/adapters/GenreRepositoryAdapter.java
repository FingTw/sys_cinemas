package com.example.cinema.admin.infrastructure.database.adapters;

import com.example.cinema.admin.domain.entities.Genre;
import com.example.cinema.admin.domain.repositories.GenreRepository;
import com.example.cinema.admin.infrastructure.database.entities.GenreJpaEntity;
import com.example.cinema.admin.infrastructure.database.repositories.SpringDataGenreRepository;
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
    public Genre save(Genre genre) {
        GenreJpaEntity entity = toEntity(genre);
        GenreJpaEntity saved = springDataGenreRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Genre> findById(String id) {
        return springDataGenreRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Genre> findByCode(String code) {
        return springDataGenreRepository.findByCode(code).map(this::toDomain);
    }

    @Override
    public List<Genre> findAll() {
        return springDataGenreRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        springDataGenreRepository.deleteById(id);
    }

    @Override
    public List<Genre> findAllByIds(List<String> ids) {
        return springDataGenreRepository.findAllById(ids).stream()
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

    private GenreJpaEntity toEntity(Genre domain) {
        return GenreJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .code(domain.getCode())
                .build();
    }
}
