package com.example.cinema.booking.application.usecases;

import com.example.cinema.booking.application.dto.DashboardStatsDTO;
import com.example.cinema.common.exception.ServerException;
import com.example.cinema.booking.application.ports.in.DashboardQueryUseCase;
import com.example.cinema.booking.domain.entities.Booking;
import com.example.cinema.booking.domain.repositories.BookingRepository;
import com.example.cinema.booking.infrastructure.feign.CatalogClient;
import com.example.cinema.booking.infrastructure.feign.ShowtimeClient;
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

    private final BookingRepository bookingRepository;
    private final CatalogClient catalogClient;

    public DashboardService(BookingRepository bookingRepository, CatalogClient catalogClient) {
        this.bookingRepository = bookingRepository;
        this.catalogClient = catalogClient;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        log.info("Dang tong hop du lieu Dashboard tu Booking Service...");
        try {
            DashboardStatsDTO stats = new DashboardStatsDTO();

            // 1. Thống kê Booking (Dữ liệu nội bộ)
            stats.setTotalBookings((long) bookingRepository.findAll().size());
            stats.setPendingBookings((long) bookingRepository.findByStatus("PENDING").size());
            stats.setConfirmedBookings((long) bookingRepository.findByStatus("CONFIRMED").size());

            // 2. Tính doanh thu
            List<Booking> confirmedBookings = bookingRepository.findAll().stream()
                    .filter(b -> "CONFIRMED".equals(b.getStatus()))
                    .collect(Collectors.toList());
            BigDecimal totalRevenue = confirmedBookings.stream()
                    .map(Booking::getTotalPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.setTotalRevenue(totalRevenue);

            // 3. Gọi Catalog Service để lấy số lượng phim (Demo Feign)
            try {
                stats.setTotalMovies(catalogClient.getMovieCount().intValue());
            } catch (Exception e) {
                log.warn("Khong the lay thong tin tu Catalog Service: {}", e.getMessage());
                stats.setTotalMovies(0);
            }

            // Cac thong tin khac tam thoi de trong hoac mac dinh
            stats.setTotalUsers(0);
            stats.setTotalShowtimes(0);
            stats.setTotalRooms(0);
            stats.setTopMovies(new ArrayList<>());

            return stats;
        } catch (Exception e) {
            log.error("Loi khi tong hop Dashboard: {}", e.getMessage(), e);
            throw new ServerException("Loi he thong: " + e.getMessage(), e);
        }
    }
}
