package com.example.cinema.booking.application.port;

import com.example.cinema.booking.application.dto.ProductDTO;
import java.util.Optional;

public interface CatalogClientPort {
    Optional<ProductDTO> getProductById(String id);
}
