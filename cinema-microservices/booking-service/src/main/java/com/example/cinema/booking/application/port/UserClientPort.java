package com.example.cinema.booking.application.port;

import com.example.cinema.booking.application.dto.UserDTO;
import java.util.Optional;

public interface UserClientPort {
    Optional<UserDTO> getUserById(String id);
}
