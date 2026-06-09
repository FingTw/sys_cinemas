package com.example.cinema.booking.infrastructure.adapters;

import com.example.cinema.booking.application.dto.feign.UserDTO;
import com.example.cinema.booking.application.ports.out.UserPort;
import com.example.cinema.booking.infrastructure.feign.UserClient;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserFeignAdapter implements UserPort {

    private final UserClient userClient;

    @Override
    public Optional<UserDTO> getUserById(String id) {
        return userClient.getUserById(id);
    }
}
