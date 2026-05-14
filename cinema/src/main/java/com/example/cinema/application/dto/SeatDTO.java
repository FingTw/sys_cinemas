package com.example.cinema.application.dto;

public class SeatDTO {
    private String id;
    private String roomId;
    private String rowLabel;
    private Integer colNumber;
    private String type;
    private String status;

    public SeatDTO() {
    }

    public SeatDTO(String id, String roomId, String rowLabel, Integer colNumber, String type, String status) {
        this.id = id;
        this.roomId = roomId;
        this.rowLabel = rowLabel;
        this.colNumber = colNumber;
        this.type = type;
        this.status = status;
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
