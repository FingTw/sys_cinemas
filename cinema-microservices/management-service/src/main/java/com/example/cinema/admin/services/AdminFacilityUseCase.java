package com.example.cinema.admin.services;

import com.example.cinema.admin.dto.RoomDTO;
import com.example.cinema.admin.dto.SeatDTO;
import java.util.List;

public interface AdminFacilityUseCase {
    RoomDTO createCustomRoom(String name, int gridRows, int gridCols, List<SeatDTO> seatDTOs, String cinemaId);
    List<RoomDTO> getAllRooms();
    void deleteRoom(String id);
    List<SeatDTO> getSeatsByRoom(String roomId);
    SeatDTO updateSeat(String seatId, String type, String status);
}
