package com.example.cinema.scheduling.application.usecases;

import com.example.cinema.scheduling.application.dto.ShowtimeDTO;
import com.example.cinema.scheduling.application.dto.ShowtimeRequest;
import com.example.cinema.scheduling.application.dto.feign.MovieDTO;
import com.example.cinema.scheduling.application.dto.feign.RoomDTO;
import com.example.cinema.scheduling.domain.entities.Showtime;
import com.example.cinema.scheduling.domain.repositories.ShowtimeRepository;
import com.example.cinema.scheduling.application.ports.out.CatalogPort;
import com.example.cinema.scheduling.application.ports.out.FacilityPort;
import com.example.cinema.scheduling.exception.SchedulingException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import com.example.cinema.scheduling.application.ports.in.ShowtimeService;
import org.modelmapper.ModelMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShowtimeServiceImpl implements ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final CatalogPort catalogPort;
    private final FacilityPort facilityPort;
    private final ModelMapper modelMapper;

    private static final int CLEANUP_MINUTES = 15;

    @Override
    @Cacheable(value = "showtimes")
    public List<ShowtimeDTO> getAllShowtimes() {
        log.info("Truy van toan bo danh sach suat chieu...");
        try {
            return showtimeRepository.findAll().stream().map(st -> {
                MovieDTO movie = st.getMovieId() != null ? catalogPort.getMovieById(st.getMovieId()).orElse(null) : null;
                RoomDTO room = st.getRoomId() != null ? facilityPort.getRoomById(st.getRoomId()).orElse(null) : null;
                return convertToDTO(st, movie, room);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw SchedulingException.databaseError("getAllShowtimes", e);
        }
    }

    @Override
    @Cacheable(value = "showtime", key = "#id")
    public ShowtimeDTO getShowtimeById(String id) {
        try {
            Showtime showtime = showtimeRepository.findById(id)
                    .orElseThrow(() -> SchedulingException.showtimeNotFound(id));
            MovieDTO movie = showtime.getMovieId() != null ? catalogPort.getMovieById(showtime.getMovieId()).orElse(null) : null;
            RoomDTO room = showtime.getRoomId() != null ? facilityPort.getRoomById(showtime.getRoomId()).orElse(null) : null;
            return convertToDTO(showtime, movie, room);
        } catch (SchedulingException e) {
            throw e;
        } catch (Exception e) {
            throw SchedulingException.databaseError("getShowtimeById(" + id + ")", e);
        }
    }

    @Override
    @Cacheable(value = "showtimesByMovie", key = "#movieId")
    public List<ShowtimeDTO> getShowtimesByMovie(String movieId) {
        log.info("Truy van suat chieu cho Phim ID: [{}]", movieId);
        try {
            List<Showtime> showtimes = showtimeRepository.findByMovieId(movieId);
            return showtimes.stream().map(st -> {
                MovieDTO movie = catalogPort.getMovieById(st.getMovieId()).orElse(null);
                RoomDTO room = st.getRoomId() != null ? facilityPort.getRoomById(st.getRoomId()).orElse(null) : null;
                return convertToDTO(st, movie, room);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw SchedulingException.databaseError("getShowtimesByMovie(" + movieId + ")", e);
        }
    }

    private ShowtimeDTO convertToDTO(Showtime st, MovieDTO movie, RoomDTO room) {
        ShowtimeDTO dto = modelMapper.map(st, ShowtimeDTO.class);
        if (movie != null) {
            dto.setMovieTitle(movie.getTitle());
            dto.setMovieDuration(movie.getDurationMinutes());
        }
        if (room != null) {
            dto.setRoomName(room.getName());
            dto.setCinemaId(room.getCinemaId());
            dto.setCinemaName(room.getCinemaName());
            dto.setCinemaComplexName(room.getCinemaComplexName());
        }
        return dto;
    }
}
