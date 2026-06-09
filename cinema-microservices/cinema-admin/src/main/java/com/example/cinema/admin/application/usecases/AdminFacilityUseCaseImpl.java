package com.example.cinema.admin.application.usecases;

import com.example.cinema.admin.application.dto.RoomDTO;
import com.example.cinema.admin.application.dto.SeatDTO;
import com.example.cinema.admin.application.ports.in.AdminFacilityUseCase;
import com.example.cinema.admin.domain.entities.Room;
import com.example.cinema.admin.domain.entities.Seat;
import com.example.cinema.admin.domain.repositories.RoomRepository;
import com.example.cinema.admin.domain.repositories.SeatRepository;
import com.example.cinema.common.exception.ServerException;
import com.example.cinema.common.exception.ClientException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.modelmapper.ModelMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminFacilityUseCaseImpl implements AdminFacilityUseCase {

    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "rooms", allEntries = true)
    })
    public RoomDTO createCustomRoom(String name, int gridRows, int gridCols, List<SeatDTO> seatDTOs) {
        log.info("Admin creating custom room [{}]: {}x{}", name, gridRows, gridCols);
        try {
            Room room = Room.builder()
                    .name(name)
                    .status("ACTIVE")
                    .gridRows(gridRows)
                    .gridCols(gridCols)
                    .build();
            Room savedRoom = roomRepository.save(room);

            List<Seat> seatsToSave = seatDTOs.stream().map(dto -> {
                String status = dto.getStatus();
                if (status == null) {
                    status = "ACTIVE";
                }
                return Seat.builder()
                        .roomId(savedRoom.getId())
                        .rowLabel(dto.getRowLabel())
                        .colNumber(dto.getColNumber())
                        .type(dto.getType())
                        .status(status)
                        .build();
            }).collect(Collectors.toList());

            seatRepository.saveAll(seatsToSave);
            log.info("Saved custom seat layout with {} seats for room [{}]", seatsToSave.size(), name);

            RoomDTO dto = modelMapper.map(savedRoom, RoomDTO.class);
            dto.setTotalSeats(seatsToSave.size());
            return dto;
        } catch (Exception e) {
            throw new ServerException("Failed to create room [" + name + "]: " + e.getMessage(), e);
        }
    }

    @Override
    public List<RoomDTO> getAllRooms() {
        try {
            return roomRepository.findAll().stream().map(room -> {
                RoomDTO dto = modelMapper.map(room, RoomDTO.class);
                dto.setTotalSeats(seatRepository.countByRoomId(room.getId()));
                return dto;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new ServerException("Failed to retrieve rooms: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "rooms", allEntries = true),
            @CacheEvict(value = "room", key = "#id"),
            @CacheEvict(value = "seats", key = "#id")
    })
    public void deleteRoom(String id) {
        log.info("Admin deleting Room ID [{}] and its seats...", id);
        try {
            seatRepository.deleteByRoomId(id);
            roomRepository.deleteById(id);
            log.info("Room and seats deleted successfully.");
        } catch (Exception e) {
            throw new ServerException("Failed to delete room [" + id + "]: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SeatDTO> getSeatsByRoom(String roomId) {
        try {
            return seatRepository.findByRoomId(roomId).stream()
                    .map(seat -> modelMapper.map(seat, SeatDTO.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ServerException("Failed to retrieve seats for room [" + roomId + "]: " + e.getMessage(), e);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "seats", allEntries = true),
            @CacheEvict(value = "rooms", allEntries = true),
            @CacheEvict(value = "room", allEntries = true)
    })
    public SeatDTO updateSeat(String seatId, String type, String status) {
        log.info("Admin updating seat ID [{}]: type={}, status={}", seatId, type, status);
        try {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new ClientException("Seat not found with ID: " + seatId));

            String newType = type != null ? type : seat.getType();
            String newStatus = status != null ? status : seat.getStatus();
            seat.updateDetails(seat.getRowLabel(), seat.getColNumber(), newType, newStatus);

            Seat updated = seatRepository.save(seat);
            return modelMapper.map(updated, SeatDTO.class);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerException("Failed to update seat [" + seatId + "]: " + e.getMessage(), e);
        }
    }
}
