package com.example.cinema.booking.infrastructure.feign;

import com.example.cinema.booking.application.dto.feign.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(name = "cinema-catalog", url = "${app.services.catalog.url}")
public interface CatalogClient {
    @GetMapping("/api/v1/public/products/{id}")
    Optional<ProductDTO> getProductById(@PathVariable("id") String id);
}
