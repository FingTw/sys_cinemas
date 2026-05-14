package com.example.cinema.booking.application.dto;

import java.math.BigDecimal;

public class SeatStatusDTO {
    private String seatId;
    private String rowLabel;
    private Integer colNumber;
    private String type;
    private String status; // AVAILABLE, HELD (Dang cho thanh toan), SOLD (Da mua)
    private BigDecimal price;

    public SeatStatusDTO() {
    }

    public SeatStatusDTO(String seatId, String rowLabel, Integer colNumber, String type, String status, BigDecimal price) {
        this.seatId = seatId;
        this.rowLabel = rowLabel;
        this.colNumber = colNumber;
        this.type = type;
        this.status = status;
        this.price = price;
    }

    // Builder manual
    public static SeatStatusDTOBuilder builder() {
        return new SeatStatusDTOBuilder();
    }

    public static class SeatStatusDTOBuilder {
        private String seatId;
        private String rowLabel;
        private Integer colNumber;
        private String type;
        private String status;
        private BigDecimal price;

        public SeatStatusDTOBuilder seatId(String seatId) {
            this.seatId = seatId;
            return this;
        }

        public SeatStatusDTOBuilder rowLabel(String rowLabel) {
            this.rowLabel = rowLabel;
            return this;
        }

        public SeatStatusDTOBuilder colNumber(Integer colNumber) {
            this.colNumber = colNumber;
            return this;
        }

        public SeatStatusDTOBuilder type(String type) {
            this.type = type;
            return this;
        }

        public SeatStatusDTOBuilder status(String status) {
            this.status = status;
            return this;
        }

        public SeatStatusDTOBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public SeatStatusDTO build() {
            return new SeatStatusDTO(seatId, rowLabel, colNumber, type, status, price);
        }
    }

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    public String getRowLabel() {
        return rowLabel;
    }

    public void setRowLabel(String rowLabel) {
        this.rowLabel = rowLabel;
    }

    public Integer getColNumber() {
        return colNumber;
    }

    public void setColNumber(Integer colNumber) {
        this.colNumber = colNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
}
