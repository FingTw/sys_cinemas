package com.example.cinema.catalog.application.usecases;

import com.example.cinema.catalog.application.dto.MovieDTO;
import com.example.cinema.catalog.domain.entities.Movie;
import com.example.cinema.catalog.domain.repositories.MovieRepository;
import com.example.cinema.catalog.exception.CatalogException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import com.example.cinema.catalog.application.ports.in.MovieService;
import org.modelmapper.ModelMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final ModelMapper modelMapper;

    @Override
    @Cacheable(value = "movies")
    public List<MovieDTO> getAllMovies() {
        log.info("Dang truy van danh sach Phim tu Database...");
        try {
            List<Movie> movies = movieRepository.findAll();
            log.info("Da tim thay {} bo phim.", movies.size());
            return movies.stream()
                    .map(movie -> modelMapper.map(movie, MovieDTO.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw CatalogException.databaseError("getAllMovies", e);
        }
    }

    @Override
    @Cacheable(value = "movie", key = "#id")
    public MovieDTO getMovieById(String id) {
        log.info("Tim kiem Phim ID: [{}]", id);
        try {
            Movie movie = movieRepository.findById(id)
                    .orElseThrow(() -> CatalogException.movieNotFound(id));
            return modelMapper.map(movie, MovieDTO.class);
        } catch (CatalogException e) {
            throw e;
        } catch (Exception e) {
            throw CatalogException.databaseError("getMovieById(" + id + ")", e);
        }
    }
}
