package com.example.cinema.admin.controllers;

import com.example.cinema.admin.entities.*;
import com.example.cinema.admin.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Public endpoints served by management-service for unauthenticated users.
 * Routes via gateway:
 *   /api/v1/public/**         → products, categories, services, promotions
 *   /api/v1/movies/**         → movies, banners, featured-movies
 *   /api/v1/showtimes/**      → showtimes (enriched with cinemaId, movieTitle...)
 *   /api/v1/facilities/**     → public complexes & cinemas
 *   /api/v1/rooms/**          → seats (Feign calls from booking-service)
 *   /api/v1/featured-movies   → featured movies list
 */
@RestController
@RequiredArgsConstructor
public class PublicDataController {

    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ServiceRepository serviceRepository;
    private final PromotionRepository promotionRepository;
    private final SeatRepository seatRepository;
    private final CinemaComplexRepository cinemaComplexRepository;
    private final CinemaRepository cinemaRepository;
    private final FeaturedMovieRepository featuredMovieRepository;
    private final RoomRepository roomRepository;

    // ── Movies ────────────────────────────────────────────────
    @Transactional(readOnly = true)
    @GetMapping("/api/v1/movies")
    public ResponseEntity<List<Map<String, Object>>> getAllMovies() {
        return ResponseEntity.ok(
                movieRepository.findAll().stream().map(this::toMovieMap).collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    @GetMapping("/api/v1/movies/{id}")
    public ResponseEntity<Map<String, Object>> getMovieById(@PathVariable String id) {
        return movieRepository.findById(id)
                .map(m -> ResponseEntity.ok(toMovieMap(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Banners: lấy từ Promotions */
    @GetMapping("/api/v1/movies/banners")
    public ResponseEntity<List<Map<String, Object>>> getBanners() {
        List<Map<String, Object>> banners = promotionRepository.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .sorted(Comparator.comparingInt(p -> p.getDisplayOrder() != null ? p.getDisplayOrder() : 0))
                .map(p -> Map.<String, Object>of(
                        "id", p.getId(),
                        "title", p.getTitle() != null ? p.getTitle() : "",
                        "imageUrl", p.getImageUrl() != null ? p.getImageUrl() : "",
                        "linkUrl", p.getLinkUrl() != null ? p.getLinkUrl() : "",
                        "displayOrder", p.getDisplayOrder() != null ? p.getDisplayOrder() : 0,
                        "createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString() : ""))
                .collect(Collectors.toList());
        return ResponseEntity.ok(banners);
    }

    /** Featured movies — dùng bởi gateway GatewayAggregationController */
    @Transactional(readOnly = true)
    @GetMapping("/api/v1/featured-movies")
    public ResponseEntity<List<Map<String, Object>>> getFeaturedMovies() {
        List<Map<String, Object>> result = featuredMovieRepository.findAll().stream()
                .filter(f -> f.getMovie() != null)
                .sorted(Comparator.comparingInt(f -> f.getDisplayOrder() != null ? f.getDisplayOrder() : 0))
                .map(f -> Map.<String, Object>of(
                        "id", f.getId(),
                        "displayOrder", f.getDisplayOrder() != null ? f.getDisplayOrder() : 0,
                        "movie", toMovieMap(f.getMovie())))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // ── Showtimes (enriched với cinemaId, movieTitle, roomName) ─
    @GetMapping("/api/v1/showtimes")
    public ResponseEntity<List<Map<String, Object>>> getAllShowtimes() {
        return ResponseEntity.ok(buildEnrichedShowtimes(showtimeRepository.findAll()));
    }

    @GetMapping("/api/v1/showtimes/{id}")
    public ResponseEntity<Map<String, Object>> getShowtimeById(@PathVariable String id) {
        return showtimeRepository.findById(id)
                .map(st -> {
                    List<Map<String, Object>> enriched = buildEnrichedShowtimes(List.of(st));
                    return enriched.isEmpty()
                            ? ResponseEntity.notFound().<Map<String, Object>>build()
                            : ResponseEntity.ok(enriched.get(0));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/v1/showtimes/movie/{movieId}")
    public ResponseEntity<List<Map<String, Object>>> getShowtimesByMovie(@PathVariable String movieId) {
        return ResponseEntity.ok(buildEnrichedShowtimes(showtimeRepository.findByMovieId(movieId)));
    }

    // ── Facilities (Public: Rạp phim) ────────────────────────
    @GetMapping("/api/v1/facilities/complexes")
    public ResponseEntity<List<CinemaComplex>> getPublicComplexes() {
        return ResponseEntity.ok(cinemaComplexRepository.findAll());
    }

    @GetMapping("/api/v1/facilities/cinemas")
    public ResponseEntity<List<Cinema>> getPublicCinemas() {
        return ResponseEntity.ok(cinemaRepository.findAll());
    }

    @GetMapping("/api/v1/facilities/complexes/{complexId}/cinemas")
    public ResponseEntity<List<Cinema>> getCinemasByComplex(@PathVariable String complexId) {
        return ResponseEntity.ok(cinemaRepository.findByComplexId(complexId));
    }

    // ── Products (Bắp nước) ──────────────────────────────────
    @GetMapping("/api/v1/public/products")
    public ResponseEntity<List<Product>> getPublicProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    @GetMapping("/api/v1/public/products/categories")
    public ResponseEntity<List<ProductCategory>> getPublicCategories() {
        return ResponseEntity.ok(productCategoryRepository.findAll());
    }

    // ── Services ─────────────────────────────────────────────
    @GetMapping("/api/v1/public/services")
    public ResponseEntity<List<Service>> getPublicServices() {
        return ResponseEntity.ok(serviceRepository.findAll());
    }

    // ── Promotions ────────────────────────────────────────────
    @GetMapping("/api/v1/public/promotions")
    public ResponseEntity<List<Promotion>> getPublicPromotions() {
        return ResponseEntity.ok(promotionRepository.findAll());
    }

    // ── Rooms (for booking-service Feign calls) ───────────────
    @GetMapping("/api/v1/rooms/{roomId}/seats")
    public ResponseEntity<List<Seat>> getSeatsByRoom(@PathVariable String roomId) {
        return ResponseEntity.ok(seatRepository.findByRoomId(roomId));
    }

    // ── Helpers ───────────────────────────────────────────────

    /**
     * Enrich showtimes với cinemaId, cinemaName, movieTitle, roomName
     * bằng cách load lookup maps 1 lần — tránh N+1 queries.
     */
    private List<Map<String, Object>> buildEnrichedShowtimes(List<Showtime> showtimes) {
        if (showtimes.isEmpty()) return List.of();

        // Build lookup maps
        Map<String, Room> roomMap = roomRepository.findAll().stream()
                .collect(Collectors.toMap(Room::getId, Function.identity(), (a, b) -> a));
        Map<String, Cinema> cinemaMap = cinemaRepository.findAll().stream()
                .collect(Collectors.toMap(Cinema::getId, Function.identity(), (a, b) -> a));
        Map<String, Movie> movieMap = movieRepository.findAll().stream()
                .collect(Collectors.toMap(Movie::getId, Function.identity(), (a, b) -> a));
        Map<String, CinemaComplex> complexMap = cinemaComplexRepository.findAll().stream()
                .collect(Collectors.toMap(CinemaComplex::getId, Function.identity(), (a, b) -> a));

        return showtimes.stream().map(st -> {
            Room room = roomMap.get(st.getRoomId());
            Cinema cinema = room != null ? cinemaMap.get(room.getCinemaId()) : null;
            Movie movie = movieMap.get(st.getMovieId());
            CinemaComplex complex = cinema != null ? complexMap.get(cinema.getComplexId()) : null;

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", st.getId());
            map.put("movieId", st.getMovieId());
            map.put("movieTitle", movie != null && movie.getTitle() != null ? movie.getTitle() : "");
            map.put("movieDuration", movie != null && movie.getDurationMinutes() != null ? movie.getDurationMinutes() : 0);
            map.put("roomId", st.getRoomId());
            map.put("roomName", room != null && room.getName() != null ? room.getName() : "");
            map.put("cinemaId", cinema != null ? cinema.getId() : "");
            map.put("cinemaName", cinema != null && cinema.getName() != null ? cinema.getName() : "");
            map.put("cinemaComplexName", complex != null && complex.getName() != null ? complex.getName() : "");
            map.put("startTime", st.getStartTime() != null ? st.getStartTime().toString() : "");
            map.put("endTime", st.getEndTime() != null ? st.getEndTime().toString() : "");
            map.put("status", st.getStatus() != null ? st.getStatus() : "");
            map.put("price", st.getPrice());
            map.put("priceVip", st.getPriceVip());
            map.put("priceCouple", st.getPriceCouple());
            return map;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    private Map<String, Object> toMovieMap(Movie m) {
        List<Map<String, Object>> genres = m.getGenres().stream()
                .map(g -> Map.<String, Object>of(
                        "id", g.getId(),
                        "name", g.getName() != null ? g.getName() : "",
                        "code", g.getCode() != null ? g.getCode() : ""))
                .collect(Collectors.toList());

        return Map.of(
                "id", m.getId(),
                "title", m.getTitle() != null ? m.getTitle() : "",
                "description", m.getDescription() != null ? m.getDescription() : "",
                "durationMinutes", m.getDurationMinutes() != null ? m.getDurationMinutes() : 0,
                "releaseDate", m.getReleaseDate() != null ? m.getReleaseDate().toString() : "",
                "posterUrl", m.getPosterUrl() != null ? m.getPosterUrl() : "",
                "status", m.getStatus() != null ? m.getStatus() : "",
                "genres", genres);
    }
}
