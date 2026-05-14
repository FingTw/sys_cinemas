package com.example.cinema.application.usecases;

import com.example.cinema.application.dto.RoomDTO;
import com.example.cinema.application.dto.SeatDTO;
import com.example.cinema.domain.entities.Room;
import com.example.cinema.domain.entities.Seat;
import com.example.cinema.domain.repositories.RoomRepository;
import com.example.cinema.domain.repositories.SeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.cinema.application.exceptions.ClientException;
import com.example.cinema.application.exceptions.ServerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FacilityUseCase {

    private static final Logger log = LoggerFactory.getLogger(FacilityUseCase.class);

    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;

    public FacilityUseCase(RoomRepository roomRepository, SeatRepository seatRepository) {
        this.roomRepository = roomRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
    public RoomDTO createCustomRoom(String name, int gridRows, int gridCols, List<SeatDTO> seatDTOs) {
        log.info("Dang tien hanh xay dung Phong chieu Custom [{}]: Kich thuoc {}x{}", name, gridRows, gridCols);

        try {
            // 1. Tao Room
            Room room = Room.builder()
                    .name(name)
                    .status("ACTIVE")
                    .gridRows(gridRows)
                    .gridCols(gridCols)
                    .build();
            Room savedRoom = roomRepository.save(room);

            // 2. Luu danh sach Seat duoc custom tu Frontend
            List<Seat> seatsToSave = seatDTOs.stream().map(dto -> Seat.builder()
                    .roomId(savedRoom.getId())
                    .rowLabel(dto.getRowLabel())
                    .colNumber(dto.getColNumber())
                    .type(dto.getType())
                    .status(dto.getStatus() != null ? dto.getStatus() : "ACTIVE")
                    .build()).collect(Collectors.toList());

            seatRepository.saveAll(seatsToSave);
            log.info("Da luu cau hinh {} ghe ngoi cho phong [{}]", seatsToSave.size(), name);

            RoomDTO dto = new RoomDTO();
            dto.setId(savedRoom.getId());
            dto.setName(savedRoom.getName());
            dto.setStatus(savedRoom.getStatus());
            dto.setGridRows(savedRoom.getGridRows());
            dto.setGridCols(savedRoom.getGridCols());
            dto.setTotalSeats(seatsToSave.size());
            return dto;
        } catch (Exception e) {
            log.error("Loi CSDL khi tao phong chieu Custom: {}", e.getMessage(), e);
            throw new ServerException("Loi he thong khi tao phong chieu: " + e.getMessage(), e);
        }
    }

    public List<RoomDTO> getAllRooms() {
        log.info("Dang truy van danh sach toan bo Phong chieu...");
        try {
            List<RoomDTO> result = roomRepository.findAll().stream().map(room -> {
                RoomDTO dto = new RoomDTO();
                dto.setId(room.getId());
                dto.setName(room.getName());
                dto.setStatus(room.getStatus());
                dto.setGridRows(room.getGridRows());
                dto.setGridCols(room.getGridCols());
                dto.setTotalSeats(seatRepository.findByRoomId(room.getId()).size());
                return dto;
            }).collect(Collectors.toList());
            log.info("Tim thay {} phong chieu.", result.size());
            return result;
        } catch (Exception e) {
            log.error("Loi CSDL khi truy van tat ca phong chieu: {}", e.getMessage(), e);
            throw new ServerException("Loi he thong khi truy xuat danh sach phong: " + e.getMessage(), e);
        }
    }

    public List<SeatDTO> getSeatsByRoom(String roomId) {
        log.info("Dang tai so do ghe cho Phong ID: [{}]...", roomId);
        try {
            List<SeatDTO> result = seatRepository.findByRoomId(roomId).stream().map(seat -> {
                SeatDTO dto = new SeatDTO();
                dto.setId(seat.getId());
                dto.setRoomId(seat.getRoomId());
                dto.setRowLabel(seat.getRowLabel());
                dto.setColNumber(seat.getColNumber());
                dto.setType(seat.getType());
                dto.setStatus(seat.getStatus());
                return dto;
            }).collect(Collectors.toList());
            log.info("Da tai {} ghe cho phong [{}].", result.size(), roomId);
            return result;
        } catch (Exception e) {
            log.error("Loi CSDL khi tai so do ghe cho phong [{}]: {}", roomId, e.getMessage(), e);
            throw new ServerException("Loi he thong khi truy xuat so do ghe: " + e.getMessage(), e);
        }
    }

    public SeatDTO updateSeat(String seatId, String type, String status) {
        log.info("Cap nhat ghe ID [{}]: Loai=[{}], Trang thai=[{}]", seatId, type, status);
        try {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new ClientException("Ghe khong ton tai"));

            String oldType = seat.getType();
            String oldStatus = seat.getStatus();
            if (type != null)
                seat.setType(type);
            if (status != null)
                seat.setStatus(status);

            Seat updated = seatRepository.save(seat);
            log.info("Da thay doi ghe [{}{}] : {} -> {}, {} -> {}",
                    updated.getRowLabel(), updated.getColNumber(),
                    oldType, updated.getType(), oldStatus, updated.getStatus());

            SeatDTO dto = new SeatDTO();
            dto.setId(updated.getId());
            dto.setRoomId(updated.getRoomId());
            dto.setRowLabel(updated.getRowLabel());
            dto.setColNumber(updated.getColNumber());
            dto.setType(updated.getType());
            dto.setStatus(updated.getStatus());
            return dto;
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Loi CSDL khi cap nhat ghe [{}]: {}", seatId, e.getMessage(), e);
            throw new ServerException("Loi he thong khi cap nhat ghe: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteRoom(String id) {
        log.info("Dang pha do Phong chieu ID [{}] va toan bo ghe ben trong...", id);
        try {
            seatRepository.deleteByRoomId(id);
            roomRepository.deleteById(id);
            log.info("Xoa phong hoan tat.");
        } catch (Exception e) {
            log.error("Loi CSDL khi xoa phong [{}]: {}", id, e.getMessage(), e);
            throw new ServerException("Loi he thong khi xoa phong: " + e.getMessage(), e);
        }
    }
}
