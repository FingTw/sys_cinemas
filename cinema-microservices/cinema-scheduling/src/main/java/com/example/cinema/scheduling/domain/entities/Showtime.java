package com.example.cinema.scheduling.domain.entities;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public class Showtime {
    private String id;
    private String movieId;
    private String roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // SCHEDULED, PLAYING, COMPLETED, CANCELLED
    private BigDecimal price;          // Giá ghế thường (STANDARD) - giữ lại tương thích
    private BigDecimal priceVip;       // Giá ghế VIP
    private BigDecimal priceCouple;    // Giá ghế COUPLE

    public Showtime() {
    }

    public Showtime(String id, String movieId, String roomId, LocalDateTime startTime,
                    LocalDateTime endTime, String status, BigDecimal price,
                    BigDecimal priceVip, BigDecimal priceCouple) {
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
    public static ShowtimeBuilder builder() {
        return new ShowtimeBuilder();
    }

    public static class ShowtimeBuilder {
        private String id;
        private String movieId;
        private String roomId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status;
        private BigDecimal price;
        private BigDecimal priceVip;
        private BigDecimal priceCouple;

        public ShowtimeBuilder id(String id) { this.id = id; return this; }
        public ShowtimeBuilder movieId(String movieId) { this.movieId = movieId; return this; }
        public ShowtimeBuilder roomId(String roomId) { this.roomId = roomId; return this; }
        public ShowtimeBuilder startTime(LocalDateTime startTime) { this.startTime = startTime; return this; }
        public ShowtimeBuilder endTime(LocalDateTime endTime) { this.endTime = endTime; return this; }
        public ShowtimeBuilder status(String status) { this.status = status; return this; }
        public ShowtimeBuilder price(BigDecimal price) { this.price = price; return this; }
        public ShowtimeBuilder priceVip(BigDecimal priceVip) { this.priceVip = priceVip; return this; }
        public ShowtimeBuilder priceCouple(BigDecimal priceCouple) { this.priceCouple = priceCouple; return this; }

        public Showtime build() {
            return new Showtime(id, movieId, roomId, startTime, endTime, status, price, priceVip, priceCouple);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getPriceVip() { return priceVip; }
    public void setPriceVip(BigDecimal priceVip) { this.priceVip = priceVip; }
    public BigDecimal getPriceCouple() { return priceCouple; }
    public void setPriceCouple(BigDecimal priceCouple) { this.priceCouple = priceCouple; }
}
