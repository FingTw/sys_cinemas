package com.example.cinema.admin.services;

import com.example.cinema.admin.dto.MovieDTO;
import java.util.List;

public interface AdminMovieUseCase {
    MovieDTO createMovie(MovieDTO dto);
    List<MovieDTO> getAllMovies();
    MovieDTO updateMovie(String id, MovieDTO dto);
    void deleteMovie(String id);
}
