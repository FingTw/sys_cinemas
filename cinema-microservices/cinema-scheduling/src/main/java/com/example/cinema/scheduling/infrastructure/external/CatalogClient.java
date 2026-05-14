package com.example.cinema.scheduling.infrastructure.external;

import com.example.cinema.scheduling.application.dto.feign.MovieDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(name = "catalog-service", url = "${app.services.catalog.url:http://localhost:8082/api/v1/movies}")
public interface CatalogClient {
    @GetMapping("/{id}")
    Optional<MovieDTO> getMovieById(@PathVariable("id") String id);
}
