package com.example.cinema.booking.adapter.persistence;

import com.example.cinema.booking.adapter.persistence.entity.BookingJpaEntity;
import com.example.cinema.booking.adapter.persistence.entity.BookingSeatJpaEntity;
import com.example.cinema.booking.adapter.persistence.entity.BookingItemJpaEntity;
import com.example.cinema.booking.domain.Booking;
import com.example.cinema.booking.domain.BookingSeat;
import com.example.cinema.booking.domain.BookingItem;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class BookingMapper {

    public Booking toDomain(BookingJpaEntity entity) {
        if (entity == null) return null;
        
        Booking domain = new Booking();
        domain.setId(entity.getId());
        domain.setUserId(entity.getUserId());
        domain.setShowtimeId(entity.getShowtimeId());
        domain.setTotalPrice(entity.getTotalPrice());
        domain.setStatus(entity.getStatus());
        domain.setExpiresAt(entity.getExpiresAt());
        domain.setPaymentTransactionId(entity.getPaymentTransactionId());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setDeleted(entity.isDeleted());
        
        if (entity.getSeats() != null) {
            domain.setSeats(entity.getSeats().stream()
                    .map(this::toDomainSeat)
                    .collect(Collectors.toList()));
            for (BookingSeat seat : domain.getSeats()) {
                seat.setBooking(domain);
            }
        }
        
        if (entity.getItems() != null) {
            domain.setItems(entity.getItems().stream()
                    .map(this::toDomainItem)
                    .collect(Collectors.toList()));
            for (BookingItem item : domain.getItems()) {
                item.setBooking(domain);
            }
        }
        
        return domain;
    }

    public BookingJpaEntity toJpa(Booking domain) {
        if (domain == null) return null;
        
        BookingJpaEntity entity = new BookingJpaEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setShowtimeId(domain.getShowtimeId());
        entity.setTotalPrice(domain.getTotalPrice());
        entity.setStatus(domain.getStatus());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setPaymentTransactionId(domain.getPaymentTransactionId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setDeleted(domain.isDeleted());
        
        if (domain.getSeats() != null) {
            entity.setSeats(domain.getSeats().stream()
                    .map(s -> toJpaSeat(s, entity))
                    .collect(Collectors.toList()));
        }
        
        if (domain.getItems() != null) {
            entity.setItems(domain.getItems().stream()
                    .map(i -> toJpaItem(i, entity))
                    .collect(Collectors.toList()));
        }
        
        return entity;
    }

    private BookingSeat toDomainSeat(BookingSeatJpaEntity entity) {
        if (entity == null) return null;
        BookingSeat seat = new BookingSeat();
        seat.setId(entity.getId());
        seat.setSeatId(entity.getSeatId());
        seat.setShowtimeId(entity.getShowtimeId());
        seat.setPrice(entity.getPrice());
        seat.setDeleted(entity.isDeleted());
        return seat;
    }

    private BookingSeatJpaEntity toJpaSeat(BookingSeat domain, BookingJpaEntity bookingEntity) {
        if (domain == null) return null;
        BookingSeatJpaEntity entity = new BookingSeatJpaEntity();
        entity.setId(domain.getId());
        entity.setBooking(bookingEntity);
        entity.setSeatId(domain.getSeatId());
        entity.setShowtimeId(domain.getShowtimeId());
        entity.setPrice(domain.getPrice());
        entity.setDeleted(domain.isDeleted());
        return entity;
    }

    private BookingItem toDomainItem(BookingItemJpaEntity entity) {
        if (entity == null) return null;
        BookingItem item = new BookingItem();
        item.setId(entity.getId());
        item.setProductId(entity.getProductId());
        item.setProductName(entity.getProductName());
        item.setQuantity(entity.getQuantity());
        item.setUnitPrice(entity.getUnitPrice());
        item.setCreatedAt(entity.getCreatedAt());
        item.setDeleted(entity.isDeleted());
        return item;
    }

    private BookingItemJpaEntity toJpaItem(BookingItem domain, BookingJpaEntity bookingEntity) {
        if (domain == null) return null;
        BookingItemJpaEntity entity = new BookingItemJpaEntity();
        entity.setId(domain.getId());
        entity.setBooking(bookingEntity);
        entity.setProductId(domain.getProductId());
        entity.setProductName(domain.getProductName());
        entity.setQuantity(domain.getQuantity());
        entity.setUnitPrice(domain.getUnitPrice());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setDeleted(domain.isDeleted());
        return entity;
    }
}
