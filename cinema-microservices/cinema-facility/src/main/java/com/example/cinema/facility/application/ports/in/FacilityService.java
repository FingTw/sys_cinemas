package com.example.cinema.facility.application.ports.in;

import com.example.cinema.facility.application.dto.RoomDTO;
import com.example.cinema.facility.application.dto.SeatDTO;
import java.util.List;

public interface FacilityService {
    List<RoomDTO> getAllRooms();
    RoomDTO getRoomById(String roomId);
    SeatDTO getSeatById(String seatId);
    List<SeatDTO> getSeatsByRoom(String roomId);
}
