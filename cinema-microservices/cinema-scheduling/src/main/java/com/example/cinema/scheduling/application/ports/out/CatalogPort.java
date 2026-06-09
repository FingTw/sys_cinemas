package com.example.cinema.scheduling.application.ports.out;

import com.example.cinema.scheduling.application.dto.feign.MovieDTO;
import java.util.Optional;

public interface CatalogPort {
    Optional<MovieDTO> getMovieById(String id);
}
