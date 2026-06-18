package com.example.cinema.admin.application.ports.in;

import com.example.cinema.admin.application.dto.RoomDTO;
import com.example.cinema.admin.application.dto.SeatDTO;
import java.util.List;

public interface AdminFacilityUseCase {
    RoomDTO createCustomRoom(String name, int gridRows, int gridCols, List<SeatDTO> seatDTOs, String cinemaId);
    List<RoomDTO> getAllRooms();
    void deleteRoom(String id);
    List<SeatDTO> getSeatsByRoom(String roomId);
    SeatDTO updateSeat(String seatId, String type, String status);
}
