package com.example.cinema.scheduling.application.usecases;

import com.example.cinema.scheduling.application.dto.ShowtimeDTO;
import com.example.cinema.scheduling.application.dto.ShowtimeRequest;
import com.example.cinema.scheduling.application.dto.feign.MovieDTO;
import com.example.cinema.scheduling.application.dto.feign.RoomDTO;
import com.example.cinema.scheduling.domain.entities.Showtime;
import com.example.cinema.scheduling.domain.repositories.ShowtimeRepository;
import com.example.cinema.scheduling.infrastructure.external.CatalogClient;
import com.example.cinema.scheduling.infrastructure.external.FacilityClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.cinema.common.exception.ClientException;
import com.example.cinema.common.exception.ServerException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShowtimeUseCase {

    private static final Logger log = LoggerFactory.getLogger(ShowtimeUseCase.class);

    private final ShowtimeRepository showtimeRepository;
    private final CatalogClient catalogClient;
    private final FacilityClient facilityClient;

    public ShowtimeUseCase(ShowtimeRepository showtimeRepository, CatalogClient catalogClient, FacilityClient facilityClient) {
        this.showtimeRepository = showtimeRepository;
        this.catalogClient = catalogClient;
        this.facilityClient = facilityClient;
    }

    private static final int CLEANUP_MINUTES = 15;

    public ShowtimeDTO createShowtime(ShowtimeRequest request) {
        try {
            // 1. Lay thong tin phim tu Catalog Service
            MovieDTO movie = catalogClient.getMovieById(request.getMovieId())
                    .orElseThrow(() -> new ClientException("Phim khong ton tai voi ID: " + request.getMovieId()));

            // 2. Lay thong tin phong tu Facility Service
            RoomDTO room = facilityClient.getRoomById(request.getRoomId())
                    .orElseThrow(() -> new ClientException("Phong chieu khong ton tai voi ID: " + request.getRoomId()));

            log.info("Yeu cau tao suat chieu: Phim [{}] tai Phong [{}] luc [{}]",
                    movie.getTitle(), room.getName(), request.getStartTime());

            // 3. Tinh endTime
            LocalDateTime endTime = request.getStartTime()
                    .plusMinutes(movie.getDurationMinutes())
                    .plusMinutes(CLEANUP_MINUTES);

            // 4. Kiem tra xung dot
            List<Showtime> conflicts = showtimeRepository.findConflicts(
                    request.getRoomId(), request.getStartTime(), endTime);

            if (!conflicts.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("XUNG DOT LICH CHIEU! Phong [").append(room.getName())
                        .append("] da co suat chieu trong khoang thoi gian nay:\n");
                for (Showtime c : conflicts) {
                    MovieDTO conflictMovie = catalogClient.getMovieById(c.getMovieId()).orElse(null);
                    String conflictTitle = conflictMovie != null ? conflictMovie.getTitle() : c.getMovieId();
                    sb.append("  - Phim [").append(conflictTitle)
                            .append("] tu ").append(c.getStartTime())
                            .append(" den ").append(c.getEndTime()).append("\n");
                }
                log.warn(sb.toString());
                throw new ClientException(sb.toString());
            }

            // 5. Luu suat chieu
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
            log.info("Tao suat chieu thanh cong: Phim [{}] tai Phong [{}], {} -> {}",
                    movie.getTitle(), room.getName(), saved.getStartTime(), saved.getEndTime());

            return mapToDTO(saved, movie, room);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Loi khi tao suat chieu: {}", e.getMessage(), e);
            throw new ServerException("Loi he thong khi tao suat chieu: " + e.getMessage(), e);
        }
    }

    public List<ShowtimeDTO> getAllShowtimes() {
        log.info("Truy van toan bo danh sach suat chieu...");
        try {
            return showtimeRepository.findAll().stream().map(st -> {
                MovieDTO movie = st.getMovieId() != null ? catalogClient.getMovieById(st.getMovieId()).orElse(null) : null;
                RoomDTO room = st.getRoomId() != null ? facilityClient.getRoomById(st.getRoomId()).orElse(null) : null;
                return mapToDTO(st, movie, room);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Loi khi lay danh sach suat chieu: {}", e.getMessage(), e);
            throw new ServerException("Loi he thong khi truy xuat lich chieu: " + e.getMessage(), e);
        }
    }

    public ShowtimeDTO getShowtimeById(String id) {
        try {
            Showtime showtime = showtimeRepository.findById(id)
                    .orElseThrow(() -> new ClientException("Suat chieu khong ton tai"));
            MovieDTO movie = showtime.getMovieId() != null ? catalogClient.getMovieById(showtime.getMovieId()).orElse(null) : null;
            RoomDTO room = showtime.getRoomId() != null ? facilityClient.getRoomById(showtime.getRoomId()).orElse(null) : null;
            return mapToDTO(showtime, movie, room);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Loi khi lay suat chieu [{}]: {}", id, e.getMessage(), e);
            throw new ServerException("Loi he thong khi truy xuat suat chieu: " + e.getMessage(), e);
        }
    }

    public void cancelShowtime(String id) {
        try {
            Showtime showtime = showtimeRepository.findById(id)
                    .orElseThrow(() -> new ClientException("Suat chieu khong ton tai"));
            showtime.setStatus("CANCELLED");
            showtimeRepository.save(showtime);
            log.info("Huy suat chieu ID: [{}]", id);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Loi khi huy suat chieu [{}]: {}", id, e.getMessage(), e);
            throw new ServerException("Loi he thong khi huy suat chieu: " + e.getMessage(), e);
        }
    }

    public void deleteShowtime(String id) {
        log.info("Xoa suat chieu ID: [{}]", id);
        try {
            showtimeRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Loi khi xoa suat chieu [{}]: {}", id, e.getMessage(), e);
            throw new ServerException("Loi he thong khi xoa suat chieu: " + e.getMessage(), e);
        }
    }

    public List<ShowtimeDTO> getShowtimesByMovie(String movieId) {
        log.info("Truy van suat chieu cho Phim ID: [{}]", movieId);
        try {
            List<Showtime> showtimes = showtimeRepository.findByMovieId(movieId);
            return showtimes.stream().map(st -> {
                MovieDTO movie = catalogClient.getMovieById(st.getMovieId()).orElse(null);
                RoomDTO room = st.getRoomId() != null ? facilityClient.getRoomById(st.getRoomId()).orElse(null) : null;
                return mapToDTO(st, movie, room);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Loi khi lay lich chieu cho phim {}: {}", movieId, e.getMessage(), e);
            throw new ServerException("Loi he thong khi truy van lich chieu: " + e.getMessage(), e);
        }
    }

    private ShowtimeDTO mapToDTO(Showtime st, MovieDTO movie, RoomDTO room) {
        ShowtimeDTO dto = new ShowtimeDTO();
        dto.setId(st.getId());
        dto.setMovieId(st.getMovieId());
        dto.setMovieTitle(movie != null ? movie.getTitle() : "N/A");
        dto.setMovieDuration(movie != null ? movie.getDurationMinutes() : 0);
        dto.setRoomId(st.getRoomId());
        dto.setRoomName(room != null ? room.getName() : "N/A");
        dto.setStartTime(st.getStartTime());
        dto.setEndTime(st.getEndTime());
        dto.setStatus(st.getStatus());
        dto.setPrice(st.getPrice());
        dto.setPriceVip(st.getPriceVip());
        dto.setPriceCouple(st.getPriceCouple());
        return dto;
    }
}
