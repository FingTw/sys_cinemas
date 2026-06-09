package com.example.cinema.booking.infrastructure.feign;

import com.example.cinema.booking.application.dto.feign.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Optional;

@FeignClient(name = "cinema-iam", url = "${app.services.iam.url}")
public interface UserClient {
    @GetMapping("/api/v1/profile/{id}")
    Optional<UserDTO> getUserById(@PathVariable("id") String id);
}
