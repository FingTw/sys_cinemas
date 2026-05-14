package com.example.cinema.facility.domain.entities;

public class Seat {
    private String id;
    private String roomId;
    private String rowLabel; // A, B, C...
    private Integer colNumber; // 1, 2, 3...
    private String type; // STANDARD, VIP, COUPLE
    private String status; // ACTIVE, BROKEN

    public Seat() {
    }

    public Seat(String id, String roomId, String rowLabel, Integer colNumber, String type, String status) {
        this.id = id;
        this.roomId = roomId;
        this.rowLabel = rowLabel;
        this.colNumber = colNumber;
        this.type = type;
        this.status = status;
    }

    // Builder manual
    public static SeatBuilder builder() {
        return new SeatBuilder();
    }

    public static class SeatBuilder {
        private String id;
        private String roomId;
        private String rowLabel;
        private Integer colNumber;
        private String type;
        private String status;

        public SeatBuilder id(String id) {
            this.id = id;
            return this;
        }

        public SeatBuilder roomId(String roomId) {
            this.roomId = roomId;
            return this;
        }

        public SeatBuilder rowLabel(String rowLabel) {
            this.rowLabel = rowLabel;
            return this;
        }

        public SeatBuilder colNumber(Integer colNumber) {
            this.colNumber = colNumber;
            return this;
        }

        public SeatBuilder type(String type) {
            this.type = type;
            return this;
        }

        public SeatBuilder status(String status) {
            this.status = status;
            return this;
        }

        public Seat build() {
            return new Seat(id, roomId, rowLabel, colNumber, type, status);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
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
}
