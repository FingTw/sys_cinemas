package com.example.cinema.booking.adapter.feign;

import com.example.cinema.booking.application.dto.ProductDTO;
import com.example.cinema.booking.application.port.CatalogClientPort;
import com.example.cinema.booking.adapter.feign.clients.CatalogClient;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CatalogClientAdapter implements CatalogClientPort {
    private final CatalogClient catalogClient;

    @Override
    public Optional<ProductDTO> getProductById(String id) {
        return catalogClient.getProductById(id);
    }
}
