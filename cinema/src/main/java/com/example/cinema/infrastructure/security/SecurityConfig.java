package com.example.cinema.infrastructure.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableAspectJAutoProxy
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApiKeyFilter apiKeyFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ApiKeyFilter apiKeyFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.apiKeyFilter = apiKeyFilter;
    }

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Cau hinh CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 2. Tat CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // 3. Phan quyen Endpoint
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/public-key",
                                "/api/v1/auth/refresh-token")
                        .permitAll()
                        .requestMatchers("/api/v1/movies/**").permitAll() // Xem danh sach phim cong khai
                        .requestMatchers("/api/v1/showtimes/**").permitAll() // Xem suat chieu cong khai
                        .requestMatchers("/api/v1/vnpay/**").permitAll() // VNPay Callback
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/v1/auth/me", "/api/v1/auth/logout").authenticated()
                        // User Management
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/users/**")
                        .hasAuthority(SecurityPermissions.USER_READ)
                        .requestMatchers("/api/v1/admin/users/**")
                        .hasAuthority(SecurityPermissions.USER_MANAGE)
                        
                        // Movie Management
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/movies/**")
                        .hasAuthority(SecurityPermissions.MOVIE_READ)
                        .requestMatchers(HttpMethod.POST, "/api/v1/admin/movies/**")
                        .hasAuthority(SecurityPermissions.MOVIE_CREATE)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/admin/movies/**")
                        .hasAuthority(SecurityPermissions.MOVIE_UPDATE)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/admin/movies/**")
                        .hasAuthority(SecurityPermissions.MOVIE_DELETE)

                        // Showtime Management
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/showtimes/**")
                        .hasAuthority(SecurityPermissions.SHOWTIME_READ)
                        .requestMatchers(HttpMethod.POST, "/api/v1/admin/showtimes/**")
                        .hasAuthority(SecurityPermissions.SHOWTIME_CREATE)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/admin/showtimes/**")
                        .hasAuthority(SecurityPermissions.SHOWTIME_UPDATE)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/admin/showtimes/**")
                        .hasAuthority(SecurityPermissions.SHOWTIME_DELETE)

                        // Facility Management (Rooms, Seats)
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/facilities/**")
                        .hasAuthority(SecurityPermissions.FACILITY_READ)
                        .requestMatchers("/api/v1/admin/facilities/**")
                        .hasAuthority(SecurityPermissions.FACILITY_MANAGE)

                        // Orders & Statistics
                        .requestMatchers("/api/v1/admin/**")
                        .hasAnyAuthority(SecurityRoles.ROLE_ADMIN, SecurityRoles.ROLE_STAFF)
                        .requestMatchers("/api/v1/orders/**")
                        .hasAnyAuthority(SecurityRoles.ROLE_ADMIN, SecurityRoles.ROLE_STAFF)
                        // Booking: User tao ve, xem lich su, huy ve (authenticated)
                        .requestMatchers("/api/v1/bookings/**").authenticated()
                        // Profile: User xem/cap nhat thong tin ca nhan (authenticated)
                        .requestMatchers("/api/v1/profile/**").authenticated()
                        .anyRequest().authenticated() // Cac API khac (dat ve, xem phim...) bat buoc phai co Token
                )
                // 4. Co che Stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 5. Them filter xac thuc
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Dinh nghia chi tiet luat CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Cho phep cac nguon tu .env
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        // Cho phep cac phuong thuc (Dac biet la OPTIONS dung cho Pre-flight cua CORS)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Ap dung luat CORS nay cho toan bo API
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
