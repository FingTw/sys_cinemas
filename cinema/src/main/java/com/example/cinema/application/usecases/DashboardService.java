package com.example.cinema.application.usecases;

import com.example.cinema.application.dto.DashboardStatsDTO;
import com.example.cinema.application.exceptions.ServerException;
import com.example.cinema.application.ports.in.DashboardQueryUseCase;
import com.example.cinema.domain.entities.Booking;
import com.example.cinema.domain.entities.Movie;
import com.example.cinema.domain.entities.Showtime;
import com.example.cinema.domain.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService implements DashboardQueryUseCase {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public DashboardService(UserRepository userRepository,
                            MovieRepository movieRepository,
                            ShowtimeRepository showtimeRepository,
                            RoomRepository roomRepository,
                            BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.showtimeRepository = showtimeRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        log.info("Dang tong hop du lieu Dashboard...");
        try {
            DashboardStatsDTO stats = new DashboardStatsDTO();

            // 1. Tổng số lượng cơ bản
            List<com.example.cinema.domain.entities.User> allUsers = userRepository.findAll();
            List<Movie> allMovies = movieRepository.findAll();
            List<Showtime> allShowtimes = showtimeRepository.findAll();
            List<com.example.cinema.domain.entities.Room> allRooms = roomRepository.findAll();

            stats.setTotalUsers(allUsers.size());
            stats.setTotalMovies(allMovies.size());
            stats.setTotalShowtimes(allShowtimes.size());
            stats.setTotalRooms(allRooms.size());

            // 2. Thống kê Booking
            stats.setTotalBookings(bookingRepository.countAll());
            stats.setPendingBookings(bookingRepository.countByStatus("PENDING"));
            stats.setConfirmedBookings(bookingRepository.countByStatus("CONFIRMED"));

            // 3. Tính doanh thu (tổng totalPrice của booking CONFIRMED)
            List<Booking> confirmedBookings = bookingRepository.findAll().stream()
                    .filter(b -> "CONFIRMED".equals(b.getStatus()))
                    .collect(Collectors.toList());
            BigDecimal totalRevenue = confirmedBookings.stream()
                    .map(Booking::getTotalPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.setTotalRevenue(totalRevenue);

            // 4. Phim theo trạng thái
            stats.setMoviesShowing(allMovies.stream()
                    .filter(m -> "SHOWING".equals(m.getStatus())).count());
            stats.setMoviesComingSoon(allMovies.stream()
                    .filter(m -> "COMING_SOON".equals(m.getStatus())).count());

            // 5. Suất chiếu theo trạng thái
            stats.setShowtimesScheduled(allShowtimes.stream()
                    .filter(s -> "SCHEDULED".equals(s.getStatus())).count());
            stats.setShowtimesPlaying(allShowtimes.stream()
                    .filter(s -> "PLAYING".equals(s.getStatus())).count());
            stats.setShowtimesCompleted(allShowtimes.stream()
                    .filter(s -> "COMPLETED".equals(s.getStatus())).count());

            // 6. Top 5 phim có nhiều booking CONFIRMED nhất
            Map<String, String> movieIdToTitle = allMovies.stream()
                    .collect(Collectors.toMap(Movie::getId, Movie::getTitle, (a, b) -> a));

            Map<String, String> showtimeToMovie = allShowtimes.stream()
                    .collect(Collectors.toMap(Showtime::getId, Showtime::getMovieId, (a, b) -> a));

            // Đếm số booking CONFIRMED theo movieId
            Map<String, Long> movieBookingCount = confirmedBookings.stream()
                    .map(b -> showtimeToMovie.getOrDefault(b.getShowtimeId(), "UNKNOWN"))
                    .collect(Collectors.groupingBy(movieId -> movieId.toString(), Collectors.counting()));

            List<DashboardStatsDTO.TopMovieDTO> topMovies = movieBookingCount.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .map(entry -> new DashboardStatsDTO.TopMovieDTO(
                            entry.getKey(),
                            movieIdToTitle.getOrDefault(entry.getKey(), "Phim da xoa"),
                            entry.getValue()))
                    .collect(Collectors.toList());
            stats.setTopMovies(topMovies);

            log.info("Dashboard tong hop xong: {} users, {} movies, {} bookings, doanh thu: {}",
                    stats.getTotalUsers(), stats.getTotalMovies(), stats.getTotalBookings(), stats.getTotalRevenue());

            return stats;
        } catch (Exception e) {
            log.error("Loi khi tong hop du lieu Dashboard: {}", e.getMessage(), e);
            throw new ServerException("Loi he thong khi tong hop du lieu Dashboard: " + e.getMessage(), e);
        }
    }
}
