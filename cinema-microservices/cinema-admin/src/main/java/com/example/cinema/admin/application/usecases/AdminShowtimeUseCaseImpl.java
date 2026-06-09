package com.example.cinema.admin.application.usecases;

import com.example.cinema.admin.application.dto.ShowtimeDTO;
import com.example.cinema.admin.application.dto.ShowtimeRequest;
import com.example.cinema.admin.application.ports.in.AdminShowtimeUseCase;
import com.example.cinema.admin.domain.entities.Movie;
import com.example.cinema.admin.domain.entities.Room;
import com.example.cinema.admin.domain.entities.Showtime;
import com.example.cinema.admin.domain.repositories.MovieRepository;
import com.example.cinema.admin.domain.repositories.RoomRepository;
import com.example.cinema.admin.domain.repositories.ShowtimeRepository;
import com.example.cinema.common.exception.ServerException;
import com.example.cinema.common.exception.ClientException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
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
public class AdminShowtimeUseCaseImpl implements AdminShowtimeUseCase {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;

    private static final int CLEANUP_MINUTES = 15;

    @Override
    @Caching(evict = {
            @CacheEvict(value = "showtimes", allEntries = true),
            @CacheEvict(value = "showtimesByMovie", allEntries = true)
    })
    public ShowtimeDTO createShowtime(ShowtimeRequest request) {
        log.info("Admin creating showtime: MovieId=[{}], RoomId=[{}], StartTime=[{}]",
                request.getMovieId(), request.getRoomId(), request.getStartTime());
        try {
            // 1. Get movie info directly from database
            Movie movie = movieRepository.findById(request.getMovieId())
                    .orElseThrow(() -> new ClientException("Movie not found with ID: " + request.getMovieId()));

            // 2. Get room info directly from database
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new ClientException("Room not found with ID: " + request.getRoomId()));

            // 3. Compute endTime
            LocalDateTime endTime = request.getStartTime()
                    .plusMinutes(movie.getDurationMinutes())
                    .plusMinutes(CLEANUP_MINUTES);

            // 4. Check conflict
            List<Showtime> conflicts = showtimeRepository.findConflicts(
                    request.getRoomId(), request.getStartTime(), endTime);

            if (!conflicts.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("SHOWTIME CONFLICT! Room [").append(room.getName())
                        .append("] already has scheduled showtimes in this slot:\n");
                for (Showtime c : conflicts) {
                    Movie conflictMovie = movieRepository.findById(c.getMovieId()).orElse(null);
                    String conflictTitle = conflictMovie != null ? conflictMovie.getTitle() : c.getMovieId();
                    sb.append("  - Movie [").append(conflictTitle)
                            .append("] from ").append(c.getStartTime())
                            .append(" to ").append(c.getEndTime()).append("\n");
                }
                log.warn(sb.toString());
                throw new ClientException(sb.toString());
            }

            // 5. Save Showtime
            Showtime showtime = Showtime.builder()
                    .movieId(request.getMovieId())
                    .roomId(request.getRoomId())
                    .startTime(request.getStartTime())
                    .endTime(endTime)
                    .status("SCHEDULED")
                    .price(request.getPrice() != null ? request.getPrice() : new BigDecimal("75000"))
                    .priceVip(request.getPriceVip() != null ? request.getPriceVip() : new BigDecimal("120000"))
                    .priceCouple(request.getPriceCouple() != null ? request.getPriceCouple() : new BigDecimal("195000"))
                    .build();

            Showtime saved = showtimeRepository.save(showtime);
            log.info("Successfully created showtime: ID=[{}]", saved.getId());

            return convertToDTO(saved, movie, room);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerException("Failed to create showtime: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ShowtimeDTO> getAllShowtimes() {
        try {
            return showtimeRepository.findAll().stream().map(st -> {
                Movie movie = movieRepository.findById(st.getMovieId()).orElse(null);
                Room room = roomRepository.findById(st.getRoomId()).orElse(null);
                return convertToDTO(st, movie, room);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new ServerException("Failed to retrieve showtimes: " + e.getMessage(), e);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "showtimes", allEntries = true),
            @CacheEvict(value = "showtime", key = "#id"),
            @CacheEvict(value = "showtimesByMovie", allEntries = true)
    })
    public void cancelShowtime(String id) {
        log.info("Admin cancelling showtime ID: [{}]", id);
        try {
            Showtime showtime = showtimeRepository.findById(id)
                    .orElseThrow(() -> new ClientException("Showtime not found with ID: " + id));
            showtime.updateDetails(showtime.getStartTime(), showtime.getEndTime(), "CANCELLED", showtime.getPrice(), showtime.getPriceVip(), showtime.getPriceCouple());
            showtimeRepository.save(showtime);
            log.info("Successfully cancelled showtime");
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerException("Failed to cancel showtime: " + e.getMessage(), e);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "showtimes", allEntries = true),
            @CacheEvict(value = "showtime", key = "#id"),
            @CacheEvict(value = "showtimesByMovie", allEntries = true)
    })
    public void deleteShowtime(String id) {
        log.info("Admin deleting showtime ID: [{}]", id);
        try {
            showtimeRepository.deleteById(id);
            log.info("Successfully deleted showtime");
        } catch (Exception e) {
            throw new ServerException("Failed to delete showtime: " + e.getMessage(), e);
        }
    }

    private ShowtimeDTO convertToDTO(Showtime st, Movie movie, Room room) {
        ShowtimeDTO dto = modelMapper.map(st, ShowtimeDTO.class);
        if (movie != null) {
            dto.setMovieTitle(movie.getTitle());
            dto.setMovieDuration(movie.getDurationMinutes());
        }
        if (room != null) {
            dto.setRoomName(room.getName());
        }
        return dto;
    }
}
