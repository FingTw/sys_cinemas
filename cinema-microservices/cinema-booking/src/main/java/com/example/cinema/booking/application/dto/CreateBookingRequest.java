package com.example.cinema.booking.application.dto;

import java.util.List;

public class CreateBookingRequest {
    private String showtimeId;
    private List<String> seatIds;

    public CreateBookingRequest() {
    }

    public CreateBookingRequest(String showtimeId, List<String> seatIds) {
        this.showtimeId = showtimeId;
        this.seatIds = seatIds;
    }

    // Builder manual
    public static CreateBookingRequestBuilder builder() {
        return new CreateBookingRequestBuilder();
    }

    public static class CreateBookingRequestBuilder {
        private String showtimeId;
        private List<String> seatIds;

        public CreateBookingRequestBuilder showtimeId(String showtimeId) {
            this.showtimeId = showtimeId;
            return this;
        }

        public CreateBookingRequestBuilder seatIds(List<String> seatIds) {
            this.seatIds = seatIds;
            return this;
        }

        public CreateBookingRequest build() {
            return new CreateBookingRequest(showtimeId, seatIds);
        }
    }

    public String getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(String showtimeId) {
        this.showtimeId = showtimeId;
    }

    public List<String> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<String> seatIds) {
        this.seatIds = seatIds;
    }
}
