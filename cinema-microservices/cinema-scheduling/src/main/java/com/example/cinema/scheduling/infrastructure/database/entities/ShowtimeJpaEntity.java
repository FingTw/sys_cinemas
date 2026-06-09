package com.example.cinema.scheduling.infrastructure.database.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "showtimes", schema = "scheduling", indexes = {
    @Index(name = "idx_showtime_movie", columnList = "movieId"),
    @Index(name = "idx_showtime_room", columnList = "roomId"),
    @Index(name = "idx_showtime_start", columnList = "startTime")
})
@SQLDelete(sql = "UPDATE scheduling.showtimes SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class ShowtimeJpaEntity {
    @jakarta.persistence.Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String movieId;

    @Column(nullable = false)
    private String roomId;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private String status = "SCHEDULED";

    @Column(nullable = true)
    private BigDecimal price = new BigDecimal("75000");

    @Column(nullable = true)
    private BigDecimal priceVip = new BigDecimal("120000");

    @Column(nullable = true)
    private BigDecimal priceCouple = new BigDecimal("195000");

    public ShowtimeJpaEntity() {
    }

    public ShowtimeJpaEntity(String id, String movieId, String roomId, LocalDateTime startTime, LocalDateTime endTime, String status, BigDecimal price, BigDecimal priceVip, BigDecimal priceCouple) {
        this.id = id;
        this.movieId = movieId;
        this.roomId = roomId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.price = price;
        this.priceVip = priceVip;
        this.priceCouple = priceCouple;
    }

    // Builder manual
    public static ShowtimeJpaEntityBuilder builder() {
        return new ShowtimeJpaEntityBuilder();
    }

    public static class ShowtimeJpaEntityBuilder {
        private String id;
        private String movieId;
        private String roomId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status = "SCHEDULED";
        private BigDecimal price = new BigDecimal("75000");
        private BigDecimal priceVip = new BigDecimal("120000");
        private BigDecimal priceCouple = new BigDecimal("195000");

        public ShowtimeJpaEntityBuilder id(String id) {
            this.id = id;
            return this;
        }

        public ShowtimeJpaEntityBuilder movieId(String movieId) {
            this.movieId = movieId;
            return this;
        }

        public ShowtimeJpaEntityBuilder roomId(String roomId) {
            this.roomId = roomId;
            return this;
        }

        public ShowtimeJpaEntityBuilder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public ShowtimeJpaEntityBuilder endTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public ShowtimeJpaEntityBuilder status(String status) {
            this.status = status;
            return this;
        }

        public ShowtimeJpaEntityBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public ShowtimeJpaEntityBuilder priceVip(BigDecimal priceVip) {
            this.priceVip = priceVip;
            return this;
        }

        public ShowtimeJpaEntityBuilder priceCouple(BigDecimal priceCouple) {
            this.priceCouple = priceCouple;
            return this;
        }

        public ShowtimeJpaEntity build() {
            return new ShowtimeJpaEntity(id, movieId, roomId, startTime, endTime, status, price, priceVip, priceCouple);
        }
    }
}
