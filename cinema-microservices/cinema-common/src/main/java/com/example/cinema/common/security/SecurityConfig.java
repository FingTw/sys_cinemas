package com.example.cinema.common.security;

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
import com.example.cinema.common.filter.AesDecryptionFilter;
import com.example.cinema.common.filter.AesEncryptionResponseFilter;
import com.example.cinema.common.filter.IdempotencyFilter;
import com.example.cinema.common.filter.XApiKeyFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableAspectJAutoProxy
public class SecurityConfig {

    private final AesDecryptionFilter aesDecryptionFilter;
    private final AesEncryptionResponseFilter aesEncryptionResponseFilter;
    private final IdempotencyFilter idempotencyFilter;
    private final XApiKeyFilter xApiKeyFilter;
    private final HeaderPermissionFilter headerPermissionFilter;

    public SecurityConfig(AesDecryptionFilter aesDecryptionFilter, AesEncryptionResponseFilter aesEncryptionResponseFilter,
                          IdempotencyFilter idempotencyFilter, XApiKeyFilter xApiKeyFilter, HeaderPermissionFilter headerPermissionFilter) {
        this.aesDecryptionFilter = aesDecryptionFilter;
        this.aesEncryptionResponseFilter = aesEncryptionResponseFilter;
        this.idempotencyFilter = idempotencyFilter;
        this.xApiKeyFilter = xApiKeyFilter;
        this.headerPermissionFilter = headerPermissionFilter;
    }

    @Value("${app.security.cors.allowed-origins}")
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
                        // Actuator health/info: public cho Docker healthcheck
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/callback", "/api/v1/auth/register", "/api/v1/auth/public-key",
                                "/api/v1/auth/refresh-token", "/api/v1/auth/password-policy",
                                "/api/v1/auth/sso/token",          // Keycloak Standalone SSO Token
                                "/api/v1/auth/check-username",     // Kiem tra ton tai username (dang ky)
                                "/api/v1/auth/check-email"         // Kiem tra ton tai email (dang ky)
                        )
                        .permitAll()
                        .requestMatchers("/api/v1/movies/**", "/api/v1/featured-movies/**", "/api/v1/public/**").permitAll() // Xem danh sach phim cong khai, promotions, services
                        .requestMatchers("/api/v1/showtimes/**").permitAll() // Xem suat chieu cong khai
                        .requestMatchers("/api/v1/facilities/**").permitAll() // Xem cum rap, rap cong khai
                        .requestMatchers(HttpMethod.GET, "/api/v1/rooms/**").permitAll()
                        .requestMatchers("/api/v1/vnpay/**").permitAll() // VNPay Callback
                        .requestMatchers(new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/error"), new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/ws/**")).permitAll()
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
                        .requestMatchers(HttpMethod.POST, "/api/v1/admin/facilities/**")
                        .hasAuthority(SecurityPermissions.FACILITY_CREATE)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/admin/facilities/**")
                        .hasAuthority(SecurityPermissions.FACILITY_UPDATE)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/admin/facilities/**")
                        .hasAuthority(SecurityPermissions.FACILITY_DELETE)

                        // Orders & Statistics
                        .requestMatchers("/api/v1/admin/**")
                        .hasAnyAuthority(SecurityRoles.ROLE_ADMIN, SecurityRoles.ROLE_STAFF)
                        .requestMatchers("/api/v1/orders/**")
                        .hasAnyAuthority(SecurityRoles.ROLE_ADMIN, SecurityRoles.ROLE_STAFF)
                        // Booking: User tao ve, xem lich su, huy ve (authenticated)
                        .requestMatchers("/api/v1/bookings/**").authenticated()
                        // Profile: User xem/cap nhat thong tin ca nhan (authenticated)
                        .requestMatchers("/api/v1/profile/**").authenticated()
                        .requestMatchers("/api/v1/internal/**").permitAll() // Internal calls between microservices
                        .anyRequest().authenticated() // Cac API khac (dat ve, xem phim...) bat buoc phai co Token
                )
                // 4. Co che Stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig::disable))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                )
                // 5. Them filter xac thuc & bao mat
                .addFilterBefore(idempotencyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(xApiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(aesDecryptionFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(aesEncryptionResponseFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(headerPermissionFilter, UsernamePasswordAuthenticationFilter.class);

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

}
