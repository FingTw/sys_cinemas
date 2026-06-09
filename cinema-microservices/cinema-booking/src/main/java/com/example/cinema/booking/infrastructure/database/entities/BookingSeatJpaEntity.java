package com.example.cinema.booking.infrastructure.database.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "booking_seats", schema = "booking", uniqueConstraints = {
    // Bao ve lop Database: Mot ghe chi duoc phep ton tai 1 lan cho 1 suat chieu
    @UniqueConstraint(name = "uk_seat_showtime", columnNames = {"seatId", "showtimeId"})
})
@Getter
@Setter
public class BookingSeatJpaEntity {
    @jakarta.persistence.Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private BookingJpaEntity booking;

    @Column(nullable = false)
    private String seatId;

    @Column(nullable = false)
    private String showtimeId;

    @Column(nullable = false)
    private BigDecimal price;

    public BookingSeatJpaEntity() {
    }

    public BookingSeatJpaEntity(String id, BookingJpaEntity booking, String seatId, String showtimeId, BigDecimal price) {
        this.id = id;
        this.booking = booking;
        this.seatId = seatId;
        this.showtimeId = showtimeId;
        this.price = price;
    }

    // Builder manual
    public static BookingSeatJpaEntityBuilder builder() {
        return new BookingSeatJpaEntityBuilder();
    }

    public static class BookingSeatJpaEntityBuilder {
        private String id;
        private BookingJpaEntity booking;
        private String seatId;
        private String showtimeId;
        private BigDecimal price;

        public BookingSeatJpaEntityBuilder id(String id) {
            this.id = id;
            return this;
        }

        public BookingSeatJpaEntityBuilder booking(BookingJpaEntity booking) {
            this.booking = booking;
            return this;
        }

        public BookingSeatJpaEntityBuilder seatId(String seatId) {
            this.seatId = seatId;
            return this;
        }

        public BookingSeatJpaEntityBuilder showtimeId(String showtimeId) {
            this.showtimeId = showtimeId;
            return this;
        }

        public BookingSeatJpaEntityBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public BookingSeatJpaEntity build() {
            return new BookingSeatJpaEntity(id, booking, seatId, showtimeId, price);
        }
    }
}
