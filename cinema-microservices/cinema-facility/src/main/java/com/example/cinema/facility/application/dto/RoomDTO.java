package com.example.cinema.facility.application.dto;

public class RoomDTO {
    private String id;
    private String name;
    private String status;
    private Integer gridRows;
    private Integer gridCols;
    private Integer totalSeats; // Computed value from seats

    public RoomDTO() {
    }

    public RoomDTO(String id, String name, String status, Integer gridRows, Integer gridCols, Integer totalSeats) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.gridRows = gridRows;
        this.gridCols = gridCols;
        this.totalSeats = totalSeats;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getGridRows() {
        return gridRows;
    }

    public void setGridRows(Integer gridRows) {
        this.gridRows = gridRows;
    }

    public Integer getGridCols() {
        return gridCols;
    }

    public void setGridCols(Integer gridCols) {
        this.gridCols = gridCols;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }
}
