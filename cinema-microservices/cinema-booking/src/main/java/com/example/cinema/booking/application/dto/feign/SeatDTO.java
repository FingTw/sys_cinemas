package com.example.cinema.booking.application.dto.feign;

public class SeatDTO {
    private String id;
    private String roomId;
    private String rowLabel;
    private int colNumber;
    private String type; // VIP, STANDARD, COUPLE
    private String status;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getRowLabel() { return rowLabel; }
    public void setRowLabel(String rowLabel) { this.rowLabel = rowLabel; }
    public int getColNumber() { return colNumber; }
    public void setColNumber(int colNumber) { this.colNumber = colNumber; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
