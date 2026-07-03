package com.example.cinema.booking.infrastructure.adapters;

import com.example.cinema.booking.application.dto.feign.ProductDTO;
import com.example.cinema.booking.application.ports.out.CatalogPort;
import com.example.cinema.booking.infrastructure.feign.CatalogClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CatalogFeignAdapter implements CatalogPort {

    private final CatalogClient catalogClient;

    @Override
    public Optional<ProductDTO> getProductById(String id) {
        return catalogClient.getProductById(id);
    }
}
