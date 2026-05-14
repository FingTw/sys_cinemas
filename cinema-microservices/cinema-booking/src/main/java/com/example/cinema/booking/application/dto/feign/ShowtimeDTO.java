package com.example.cinema.booking.application.dto.feign;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShowtimeDTO {
    private String id;
    private String movieId;
    private String movieTitle;
    private Integer movieDuration;
    private String roomId;
    private String roomName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal price;
    private BigDecimal priceVip;
    private BigDecimal priceCouple;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    public Integer getMovieDuration() { return movieDuration; }
    public void setMovieDuration(Integer movieDuration) { this.movieDuration = movieDuration; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getPriceVip() { return priceVip; }
    public void setPriceVip(BigDecimal priceVip) { this.priceVip = priceVip; }
    public BigDecimal getPriceCouple() { return priceCouple; }
    public void setPriceCouple(BigDecimal priceCouple) { this.priceCouple = priceCouple; }
}
