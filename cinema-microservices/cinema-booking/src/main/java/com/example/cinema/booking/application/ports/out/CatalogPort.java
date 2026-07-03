package com.example.cinema.booking.application.ports.out;

import com.example.cinema.booking.application.dto.feign.ProductDTO;
import java.util.Optional;

public interface CatalogPort {
    Optional<ProductDTO> getProductById(String id);
}
