package com.example.cinema.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class GatewayAggregationController {

    private final WebClient webClient;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String CACHE_KEY = "cache:home-overview";

    @Value("${CATALOG_SERVICE_URL}")
    private String catalogServiceUrl;

    @Value("${APP_SECURITY_INTERNAL_API_KEY:my-secret-dev-api-key}")
    private String internalApiKey;

    public GatewayAggregationController(WebClient.Builder webClientBuilder,
                                        ReactiveStringRedisTemplate redisTemplate,
                                        ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * API BFF (Backend For Frontend) dành riêng cho trang chủ (Home Page).
     * Thay vì Frontend phải tự gọi list movies rồi tự cắt mảng, tìm phim nổi bật...
     * Gateway sẽ gọi Catalog Service, xử lý sẵn dữ liệu theo đúng "khẩu vị" của View và trả về 1 cục JSON hoàn chỉnh.
     */
    @GetMapping("/home-overview")
    public Mono<HomeOverviewDto> getHomeOverview() {
        // 1. Kiểm tra Cache trước
        return redisTemplate.opsForValue().get(CACHE_KEY)
                .flatMap(cachedData -> {
                    try {
                        return Mono.just(objectMapper.readValue(cachedData, HomeOverviewDto.class));
                    } catch (Exception e) {
                        return Mono.empty(); // Nếu parse lỗi, coi như miss cache
                    }
                })
                .switchIfEmpty(fetchFromDownstreamAndCache());
    }

    private Mono<HomeOverviewDto> fetchFromDownstreamAndCache() {
        Mono<List<Map>> moviesMono = webClient.get()
                .uri(catalogServiceUrl + "/api/v1/movies")
                .header("X-API-Key", internalApiKey)
                .retrieve()
                .bodyToFlux(Map.class)
                .collectList()
                .onErrorReturn(List.of());

        Mono<List<Map>> featuredMoviesMono = webClient.get()
                .uri(catalogServiceUrl + "/api/v1/featured-movies")
                .header("X-API-Key", internalApiKey)
                .retrieve()
                .bodyToFlux(Map.class)
                .collectList()
                .onErrorReturn(List.of());

        return Mono.zip(moviesMono, featuredMoviesMono)
                .map(tuple -> {
                    List<Map> movies = tuple.getT1();
                    List<Map> featuredItems = tuple.getT2();

                    HomeOverviewDto response = new HomeOverviewDto();
                    response.setMovies(movies);

                    List<Map> topMovies;
                    if (!featuredItems.isEmpty()) {
                        // Trích xuất thuộc tính "movie" từ FeaturedMovieDTO
                        topMovies = featuredItems.stream()
                                .map(f -> (Map) f.get("movie"))
                                .limit(4)
                                .collect(Collectors.toList());
                    } else {
                        // Fallback về 4 phim đang chiếu
                        topMovies = movies.stream()
                                .filter(m -> "SHOWING".equals(m.get("status")))
                                .limit(4)
                                .collect(Collectors.toList());
                    }
                    response.setTopMovies(topMovies);

                    Map featured = topMovies.isEmpty() ? null : topMovies.get(0);
                    response.setFeaturedMovie(featured);

                    return response;
                })
                .flatMap(dto -> {
                    // 2. Lưu vào Cache với TTL 5 giây
                    try {
                        String json = objectMapper.writeValueAsString(dto);
                        return redisTemplate.opsForValue().set(CACHE_KEY, json, Duration.ofSeconds(5))
                                .thenReturn(dto);
                    } catch (JsonProcessingException e) {
                        return Mono.just(dto);
                    }
                });
    }

    public static class HomeOverviewDto {
        private List<Map> movies;
        private List<Map> topMovies;
        private Map featuredMovie;

        public HomeOverviewDto() {}

        public HomeOverviewDto(List<Map> movies, List<Map> topMovies, Map featuredMovie) {
            this.movies = movies;
            this.topMovies = topMovies;
            this.featuredMovie = featuredMovie;
        }

        public List<Map> getMovies() { return movies; }
        public void setMovies(List<Map> movies) { this.movies = movies; }

        public List<Map> getTopMovies() { return topMovies; }
        public void setTopMovies(List<Map> topMovies) { this.topMovies = topMovies; }

        public Map getFeaturedMovie() { return featuredMovie; }
        public void setFeaturedMovie(Map featuredMovie) { this.featuredMovie = featuredMovie; }
    }
}
