package com.example.cinema.booking.infrastructure.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Optional;
import java.util.Map;

@FeignClient(name = "cinema-catalog", url = "${app.services.catalog.url:http://localhost:8082}")
public interface CatalogClient {
    @GetMapping("/api/v1/movies/stats/count")
    Long getMovieCount();
}
