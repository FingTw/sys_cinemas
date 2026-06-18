package com.example.cinema.facility.application.usecases;

import com.example.cinema.facility.application.dto.RoomDTO;
import com.example.cinema.facility.application.dto.SeatDTO;
import com.example.cinema.facility.domain.entities.Room;
import com.example.cinema.facility.domain.entities.Seat;
import com.example.cinema.facility.domain.entities.Cinema;
import com.example.cinema.facility.domain.entities.CinemaComplex;
import com.example.cinema.facility.domain.repositories.RoomRepository;
import com.example.cinema.facility.domain.repositories.SeatRepository;
import com.example.cinema.facility.domain.repositories.CinemaRepository;
import com.example.cinema.facility.domain.repositories.CinemaComplexRepository;
import com.example.cinema.facility.exception.FacilityException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.cinema.facility.application.ports.in.FacilityService;
import org.modelmapper.ModelMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityServiceImpl implements FacilityService {

    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final CinemaRepository cinemaRepository;
    private final CinemaComplexRepository cinemaComplexRepository;
    private final ModelMapper modelMapper;

    @Override
    @Cacheable(value = "rooms")
    public List<RoomDTO> getAllRooms() {
        log.info("Dang truy van danh sach toan bo Phong chieu...");
        try {
            List<RoomDTO> result = roomRepository.findAll().stream().map(room -> {
                RoomDTO dto = modelMapper.map(room, RoomDTO.class);
                dto.setTotalSeats(seatRepository.countByRoomId(room.getId()));
                enrichRoomDTO(dto);
                return dto;
            }).collect(Collectors.toList());
            log.info("Tim thay {} phong chieu.", result.size());
            return result;
        } catch (Exception e) {
            throw FacilityException.databaseError("getAllRooms", e);
        }
    }

    @Override
    @Cacheable(value = "room", key = "#roomId")
    public RoomDTO getRoomById(String roomId) {
        log.info("Dang truy van phong chieu ID: [{}]...", roomId);
        try {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> FacilityException.roomNotFound(roomId));

            RoomDTO dto = modelMapper.map(room, RoomDTO.class);
            dto.setTotalSeats(seatRepository.countByRoomId(room.getId()));
            enrichRoomDTO(dto);
            return dto;
        } catch (FacilityException e) {
            throw e;
        } catch (Exception e) {
            throw FacilityException.databaseError("getRoomById(" + roomId + ")", e);
        }
    }

    private void enrichRoomDTO(RoomDTO dto) {
        if (dto.getCinemaId() != null) {
            cinemaRepository.findById(dto.getCinemaId()).ifPresent(c -> {
                dto.setCinemaName(c.getName());
                cinemaComplexRepository.findById(c.getComplexId()).ifPresent(cc -> {
                    dto.setCinemaComplexName(cc.getName());
                });
            });
        }
    }

    @Override
    public SeatDTO getSeatById(String seatId) {
        log.info("Dang truy van ghe ID: [{}]...", seatId);
        try {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> FacilityException.seatNotFound(seatId));

            return modelMapper.map(seat, SeatDTO.class);
        } catch (FacilityException e) {
            throw e;
        } catch (Exception e) {
            throw FacilityException.databaseError("getSeatById(" + seatId + ")", e);
        }
    }

    @Override
    @Cacheable(value = "seats", key = "#roomId")
    public List<SeatDTO> getSeatsByRoom(String roomId) {
        log.info("Dang tai so do ghe cho Phong ID: [{}]...", roomId);
        try {
            List<SeatDTO> result = seatRepository.findByRoomId(roomId).stream()
                    .map(seat -> modelMapper.map(seat, SeatDTO.class))
                    .collect(Collectors.toList());
            log.info("Da tai {} ghe cho phong [{}].", result.size(), roomId);
            return result;
        } catch (Exception e) {
            throw FacilityException.databaseError("getSeatsByRoom(" + roomId + ")", e);
        }
    }
}
