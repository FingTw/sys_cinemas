package com.example.cinema.application.usecases;

import com.example.cinema.application.dto.ShowtimeDTO;
import com.example.cinema.application.dto.ShowtimeRequest;
import com.example.cinema.domain.entities.Movie;
import com.example.cinema.domain.entities.Room;
import com.example.cinema.domain.entities.Showtime;
import com.example.cinema.domain.repositories.MovieRepository;
import com.example.cinema.domain.repositories.RoomRepository;
import com.example.cinema.domain.repositories.ShowtimeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.cinema.application.exceptions.ClientException;
import com.example.cinema.application.exceptions.ServerException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShowtimeUseCase {

    private static final Logger log = LoggerFactory.getLogger(ShowtimeUseCase.class);

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;

    public ShowtimeUseCase(ShowtimeRepository showtimeRepository, MovieRepository movieRepository, RoomRepository roomRepository) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
    }

    private static final int CLEANUP_MINUTES = 15; // Thoi gian don rap sau moi suat chieu

    public ShowtimeDTO createShowtime(ShowtimeRequest request) {
        try {
            // 1. Lay thong tin phim de tinh thoi luong
            Movie movie = movieRepository.findById(request.getMovieId())
                    .orElseThrow(() -> new ClientException("Phim khong ton tai voi ID: " + request.getMovieId()));

            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new ClientException("Phong chieu khong ton tai voi ID: " + request.getRoomId()));

            log.info("Yeu cau tao suat chieu: Phim [{}] tai Phong [{}] luc [{}]",
                    movie.getTitle(), room.getName(), request.getStartTime());

            // 2. Tinh endTime = startTime + durationMinutes + 15 phut don rap
            LocalDateTime endTime = request.getStartTime()
                    .plusMinutes(movie.getDurationMinutes())
                    .plusMinutes(CLEANUP_MINUTES);

            // 3. Kiem tra xung dot (Conflict Detection)
            List<Showtime> conflicts = showtimeRepository.findConflicts(
                    request.getRoomId(), request.getStartTime(), endTime);

            if (!conflicts.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("XUNG DOT LICH CHIEU! Phong [").append(room.getName())
                        .append("] da co suat chieu trong khoang thoi gian nay:\n");
                for (Showtime c : conflicts) {
                    Movie conflictMovie = movieRepository.findById(c.getMovieId()).orElse(null);
                    String conflictTitle = conflictMovie != null ? conflictMovie.getTitle() : c.getMovieId();
                    sb.append("  - Phim [").append(conflictTitle)
                            .append("] tu ").append(c.getStartTime())
                            .append(" den ").append(c.getEndTime()).append("\n");
                }
                log.warn(sb.toString());
                throw new ClientException(sb.toString());
            }

            // 4. Khong co xung dot -> Luu suat chieu moi
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
            log.error("Loi CSDL khi tao suat chieu: {}", e.getMessage(), e);
            throw new ServerException("Loi he thong khi tao suat chieu: " + e.getMessage(), e);
        }
    }

    public List<ShowtimeDTO> getAllShowtimes() {
        log.info("Truy van toan bo danh sach suat chieu...");
        try {
            return showtimeRepository.findAll().stream().map(st -> {
                Movie movie = st.getMovieId() != null ? movieRepository.findById(st.getMovieId()).orElse(null) : null;
                Room room = st.getRoomId() != null ? roomRepository.findById(st.getRoomId()).orElse(null) : null;
                return mapToDTO(st, movie, room);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Loi CSDL khi lay danh sach suat chieu: {}", e.getMessage(), e);
            throw new ServerException("Loi he thong khi truy xuat lich chieu: " + e.getMessage(), e);
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
            log.error("Loi CSDL khi huy suat chieu [{}]: {}", id, e.getMessage(), e);
            throw new ServerException("Loi he thong khi huy suat chieu: " + e.getMessage(), e);
        }
    }

    public void deleteShowtime(String id) {
        log.info("Xoa suat chieu ID: [{}]", id);
        try {
            showtimeRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Loi CSDL khi xoa suat chieu [{}]: {}", id, e.getMessage(), e);
            throw new ServerException("Loi he thong khi xoa suat chieu: " + e.getMessage(), e);
        }
    }

    public List<ShowtimeDTO> getShowtimesByMovie(String movieId) {
        log.info("Truy van suat chieu cho Phim ID: [{}]", movieId);
        try {
            List<Showtime> showtimes = showtimeRepository.findByMovieId(movieId);
            log.info("Tim thay {} suat chieu cho phim {}", showtimes.size(), movieId);
            return showtimes.stream().map(st -> {
                Movie movie = st.getMovieId() != null ? movieRepository.findById(st.getMovieId()).orElse(null) : null;
                Room room = st.getRoomId() != null ? roomRepository.findById(st.getRoomId()).orElse(null) : null;
                return mapToDTO(st, movie, room);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Loi khi lay lich chieu cho phim {}: {}", movieId, e.getMessage(), e);
            throw new ServerException("Loi he thong khi truy van lich chieu: " + e.getMessage(), e);
        }
    }

    private ShowtimeDTO mapToDTO(Showtime st, Movie movie, Room room) {
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
        dto.setPrice(st.getPrice() != null ? st.getPrice() : new BigDecimal("75000"));
        dto.setPriceVip(st.getPriceVip() != null ? st.getPriceVip() : new BigDecimal("120000"));
        dto.setPriceCouple(st.getPriceCouple() != null ? st.getPriceCouple() : new BigDecimal("195000"));
        return dto;
    }
}
