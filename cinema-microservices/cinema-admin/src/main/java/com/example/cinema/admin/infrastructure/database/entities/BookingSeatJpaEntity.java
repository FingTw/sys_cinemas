package com.example.cinema.admin.infrastructure.database.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "booking_seats", schema = "booking", uniqueConstraints = {
    @UniqueConstraint(name = "uk_seat_showtime", columnNames = {"seatId", "showtimeId"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingSeatJpaEntity {
    @Builder.Default
    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Id
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
}
