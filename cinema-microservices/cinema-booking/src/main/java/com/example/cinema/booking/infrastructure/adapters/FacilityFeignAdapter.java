package com.example.cinema.booking.infrastructure.adapters;

import com.example.cinema.booking.application.dto.feign.SeatDTO;
import com.example.cinema.booking.application.ports.out.FacilityPort;
import com.example.cinema.booking.infrastructure.feign.FacilityClient;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FacilityFeignAdapter implements FacilityPort {

    private final FacilityClient facilityClient;

    @Override
    public Optional<SeatDTO> getSeatById(String id) {
        return facilityClient.getSeatById(id);
    }

    @Override
    public List<SeatDTO> getSeatsByRoomId(String roomId) {
        return facilityClient.getSeatsByRoomId(roomId);
    }
}
