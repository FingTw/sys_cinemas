package com.example.cinema.booking.infrastructure.feign;

import com.example.cinema.booking.application.dto.feign.SeatDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Optional;
import java.util.List;

@FeignClient(name = "cinema-facility", url = "${app.services.facility.url:http://localhost:8083}")
public interface FacilityClient {
    @GetMapping("/api/v1/rooms/seats/{id}")
    Optional<SeatDTO> getSeatById(@PathVariable("id") String id);
    
    @GetMapping("/api/v1/rooms/{roomId}/seats")
    List<SeatDTO> getSeatsByRoomId(@PathVariable("roomId") String roomId);
}
