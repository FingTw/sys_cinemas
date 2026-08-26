package com.example.cinema.booking.adapter.feign;

import com.example.cinema.booking.application.dto.UserDTO;
import com.example.cinema.booking.application.port.UserClientPort;
import com.example.cinema.booking.adapter.feign.clients.UserClient;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserClientAdapter implements UserClientPort {
    private final UserClient userClient;

    @Override
    public Optional<UserDTO> getUserById(String id) {
        return userClient.getUserById(id);
    }
}
