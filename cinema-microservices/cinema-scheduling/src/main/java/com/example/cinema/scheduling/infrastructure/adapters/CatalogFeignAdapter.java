package com.example.cinema.scheduling.infrastructure.adapters;

import com.example.cinema.scheduling.application.dto.feign.MovieDTO;
import com.example.cinema.scheduling.application.ports.out.CatalogPort;
import com.example.cinema.scheduling.infrastructure.external.CatalogClient;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CatalogFeignAdapter implements CatalogPort {

    private final CatalogClient catalogClient;

    @Override
    public Optional<MovieDTO> getMovieById(String id) {
        return catalogClient.getMovieById(id);
    }
}
