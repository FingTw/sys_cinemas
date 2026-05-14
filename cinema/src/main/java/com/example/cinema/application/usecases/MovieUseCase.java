package com.example.cinema.application.usecases;

import com.example.cinema.application.dto.MovieDTO;
import com.example.cinema.domain.entities.Movie;
import com.example.cinema.domain.repositories.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.cinema.application.exceptions.ClientException;
import com.example.cinema.application.exceptions.ServerException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieUseCase {

    private static final Logger log = LoggerFactory.getLogger(MovieUseCase.class);

    private final MovieRepository movieRepository;

    public MovieUseCase(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public MovieDTO createMovie(MovieDTO dto) {
        try {
            Movie movie = Movie.builder()
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .durationMinutes(dto.getDurationMinutes())
                    .releaseDate(dto.getReleaseDate())
                    .posterUrl(dto.getPosterUrl())
                    .genre(dto.getGenre())
                    .status(dto.getStatus() != null ? dto.getStatus() : "COMING_SOON")
                    .build();
            Movie saved = movieRepository.save(movie);
            log.info("Da luu Phim moi vao Database: [{}] - ID: [{}]", saved.getTitle(), saved.getId());
            return mapToDTO(saved);
        } catch (Exception e) {
            log.error("Loi CSDL khi tao Phim moi: {}", e.getMessage(), e);
            throw new ServerException("Loi he thong khi tao Phim: " + e.getMessage(), e);
        }
    }

    public List<MovieDTO> getAllMovies() {
        log.info("Dang truy van danh sach Phim tu Database...");
        try {
            List<Movie> movies = movieRepository.findAll();
            log.info("Da tim thay {} bo phim.", movies.size());
            return movies.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Loi CSDL khi lay danh sach Phim: {}", e.getMessage(), e);
            throw new ServerException("Loi he thong khi truy xuat danh sach Phim: " + e.getMessage(), e);
        }
    }

    public MovieDTO updateMovie(String id, MovieDTO dto) {
        try {
            Movie movie = movieRepository.findById(id)
                    .orElseThrow(() -> new ClientException("Phim khong ton tai!"));

            movie.setTitle(dto.getTitle());
            movie.setDescription(dto.getDescription());
            movie.setDurationMinutes(dto.getDurationMinutes());
            movie.setReleaseDate(dto.getReleaseDate());
            movie.setPosterUrl(dto.getPosterUrl());
            movie.setGenre(dto.getGenre());
            movie.setStatus(dto.getStatus());

            Movie updated = movieRepository.save(movie);
            log.info("Da cap nhat thanh cong Phim ID: [{}]", updated.getId());
            return mapToDTO(updated);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Loi CSDL khi cap nhat Phim ID [{}]: {}", id, e.getMessage(), e);
            throw new ServerException("Loi he thong khi cap nhat Phim: " + e.getMessage(), e);
        }
    }

    public MovieDTO getMovieById(String id) {
        log.info("Tim kiem Phim ID: [{}]", id);
        try {
            Movie movie = movieRepository.findById(id)
                    .orElseThrow(() -> new ClientException("Phim khong ton tai!"));
            return mapToDTO(movie);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Loi CSDL khi tim Phim ID [{}]: {}", id, e.getMessage(), e);
            throw new ServerException("Loi he thong khi tim Phim: " + e.getMessage(), e);
        }
    }

    public void deleteMovie(String id) {
        log.info("Dang xoa Phim ID: [{}] khoi Database...", id);
        try {
            movieRepository.deleteById(id);
            log.info("Da xoa thanh cong!");
        } catch (Exception e) {
            log.error("Loi CSDL khi xoa Phim ID [{}]: {}", id, e.getMessage(), e);
            throw new ServerException("Loi he thong khi xoa Phim: " + e.getMessage(), e);
        }
    }

    private MovieDTO mapToDTO(Movie movie) {
        MovieDTO dto = new MovieDTO();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setDescription(movie.getDescription());
        dto.setDurationMinutes(movie.getDurationMinutes());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setPosterUrl(movie.getPosterUrl());
        dto.setGenre(movie.getGenre());
        dto.setStatus(movie.getStatus());
        return dto;
    }
}
