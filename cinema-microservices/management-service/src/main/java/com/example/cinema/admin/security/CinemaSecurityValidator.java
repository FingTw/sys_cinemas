package com.example.cinema.admin.security;

import com.example.cinema.admin.repositories.RoomRepository;
import com.example.cinema.admin.repositories.ShowtimeRepository;
import com.example.cinema.admin.entities.Room;
import com.example.cinema.admin.entities.Showtime;
import com.example.cinema.common.security.CinemaSecurity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("cinemaSecurityValidator")
@RequiredArgsConstructor
public class CinemaSecurityValidator {

    private final CinemaSecurity cinemaSecurity;
    private final RoomRepository roomRepository;
    private final ShowtimeRepository showtimeRepository;

    public boolean hasAccessToCinema(Authentication authentication, String cinemaId) {
        return cinemaSecurity.hasAccess(authentication, cinemaId);
    }

    public boolean hasAccessToRoom(Authentication authentication, String roomId) {
        if (roomId == null) return false;
        
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isEmpty()) {
            // Allow bypassing if the room doesn't exist so the standard 404 can handle it
            return true;
        }
        return cinemaSecurity.hasAccess(authentication, roomOpt.get().getCinemaId());
    }

    public boolean hasAccessToShowtime(Authentication authentication, String showtimeId) {
        if (showtimeId == null) return false;

        Optional<Showtime> showtimeOpt = showtimeRepository.findById(showtimeId);
        if (showtimeOpt.isEmpty()) {
            return true;
        }
        
        String roomId = showtimeOpt.get().getRoomId();
        return hasAccessToRoom(authentication, roomId);
    }
}
