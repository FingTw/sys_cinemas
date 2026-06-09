package com.example.cinema.catalog.application.ports.in;

import com.example.cinema.catalog.application.dto.MovieDTO;
import java.util.List;

public interface MovieService {
    List<MovieDTO> getAllMovies();
    MovieDTO getMovieById(String id);
}
