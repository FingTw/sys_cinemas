package com.example.cinema.infrastructure.database.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "showtimes", schema = "scheduling")
public class ShowtimeJpaEntity {
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPriceVip() {
        return priceVip;
    }

    public void setPriceVip(BigDecimal priceVip) {
        this.priceVip = priceVip;
    }

    public BigDecimal getPriceCouple() {
        return priceCouple;
    }

    public void setPriceCouple(BigDecimal priceCouple) {
        this.priceCouple = priceCouple;
    }
}
