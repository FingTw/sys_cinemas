package com.example.cinema.scheduling.application.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public class ShowtimeRequest {
    private String movieId;
    private String roomId;
    private LocalDateTime startTime;
    private BigDecimal price;
    private BigDecimal priceVip;
    private BigDecimal priceCouple;

    public ShowtimeRequest() {
    }

    public ShowtimeRequest(String movieId, String roomId, LocalDateTime startTime, BigDecimal price, BigDecimal priceVip, BigDecimal priceCouple) {
        this.movieId = movieId;
        this.roomId = roomId;
        this.startTime = startTime;
        this.price = price;
        this.priceVip = priceVip;
        this.priceCouple = priceCouple;
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
