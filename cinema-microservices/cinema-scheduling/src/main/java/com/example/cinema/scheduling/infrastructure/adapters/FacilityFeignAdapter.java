package com.example.cinema.scheduling.infrastructure.adapters;

import com.example.cinema.scheduling.application.dto.feign.RoomDTO;
import com.example.cinema.scheduling.application.ports.out.FacilityPort;
import com.example.cinema.scheduling.infrastructure.external.FacilityClient;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FacilityFeignAdapter implements FacilityPort {

    private final FacilityClient facilityClient;

    @Override
    public Optional<RoomDTO> getRoomById(String id) {
        return facilityClient.getRoomById(id);
    }
}
