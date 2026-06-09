package com.example.cinema.scheduling.infrastructure.external;

import com.example.cinema.scheduling.application.dto.feign.RoomDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(name = "facility-service", url = "${app.services.facility.url}")
public interface FacilityClient {
    @GetMapping("/{id}")
    Optional<RoomDTO> getRoomById(@PathVariable("id") String id);
}
