package com.example.cinema.admin.application.usecases;

import com.example.cinema.admin.application.dto.GenreDTO;
import com.example.cinema.admin.application.ports.in.AdminGenreUseCase;
import com.example.cinema.admin.domain.entities.Genre;
import com.example.cinema.admin.domain.repositories.GenreRepository;
import com.example.cinema.common.exception.ServerException;
import com.example.cinema.common.exception.ClientException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminGenreUseCaseImpl implements AdminGenreUseCase {

    private final GenreRepository genreRepository;
    private final ModelMapper modelMapper;

    @Override
    @Caching(evict = {
            @CacheEvict(value = "genres", allEntries = true)
    })
    public GenreDTO createGenre(GenreDTO dto) {
        try {
            if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                throw new ClientException("Genre name cannot be empty");
            }
            Genre genre = Genre.builder()
                    .name(dto.getName())
                    .code(dto.getCode())
                    .build();

            // Kiểm tra trùng mã code
            if (genreRepository.findByCode(genre.getCode()).isPresent()) {
                throw new ClientException("Genre with code " + genre.getCode() + " already exists");
            }

            Genre saved = genreRepository.save(genre);
            log.info("Admin created new Genre: [{}] - Code: [{}]", saved.getName(), saved.getCode());
            return modelMapper.map(saved, GenreDTO.class);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerException("Genre creation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<GenreDTO> getAllGenres() {
        try {
            return genreRepository.findAll().stream()
                    .map(genre -> modelMapper.map(genre, GenreDTO.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ServerException("Failed to retrieve genres: " + e.getMessage(), e);
        }
    }

    @Override
    public GenreDTO getGenreById(String id) {
        try {
            Genre genre = genreRepository.findById(id)
                    .orElseThrow(() -> new ClientException("Genre not found with ID: " + id));
            return modelMapper.map(genre, GenreDTO.class);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerException("Failed to retrieve genre: " + e.getMessage(), e);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "genres", allEntries = true),
            @CacheEvict(value = "genre", key = "#id")
    })
    public GenreDTO updateGenre(String id, GenreDTO dto) {
        try {
            Genre genre = genreRepository.findById(id)
                    .orElseThrow(() -> new ClientException("Genre not found with ID: " + id));

            genre.updateDetails(dto.getName(), dto.getCode());

            // Kiểm tra trùng mã code khi cập nhật (nếu code thay đổi)
            genreRepository.findByCode(genre.getCode()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new ClientException("Genre with code " + genre.getCode() + " already exists");
                }
            });

            Genre updated = genreRepository.save(genre);
            log.info("Successfully updated Genre ID: [{}]", updated.getId());
            return modelMapper.map(updated, GenreDTO.class);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerException("Genre update failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "genres", allEntries = true),
            @CacheEvict(value = "genre", key = "#id")
    })
    public void deleteGenre(String id) {
        log.info("Deleting Genre ID: [{}] from Database...", id);
        try {
            genreRepository.deleteById(id);
            log.info("Successfully deleted genre!");
        } catch (Exception e) {
            throw new ServerException("Genre deletion failed: " + e.getMessage(), e);
        }
    }
}
