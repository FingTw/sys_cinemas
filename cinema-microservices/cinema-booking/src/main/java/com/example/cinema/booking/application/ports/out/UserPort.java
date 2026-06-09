package com.example.cinema.booking.application.ports.out;

import com.example.cinema.booking.application.dto.feign.UserDTO;
import java.util.Optional;

public interface UserPort {
    Optional<UserDTO> getUserById(String id);
}
