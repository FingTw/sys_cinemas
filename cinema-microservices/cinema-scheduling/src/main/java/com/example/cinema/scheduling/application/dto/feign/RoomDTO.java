package com.example.cinema.scheduling.application.dto.feign;

public class RoomDTO {
    private String id;
    private String name;
    private Integer totalSeats;
    private String type;
    private boolean isActive;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
