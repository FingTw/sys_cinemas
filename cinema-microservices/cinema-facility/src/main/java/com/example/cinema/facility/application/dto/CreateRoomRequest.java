package com.example.cinema.facility.application.dto;

import java.util.List;

public class CreateRoomRequest {
    private String name;
    private Integer gridRows;
    private Integer gridCols;
    private List<SeatDTO> seats;

    public CreateRoomRequest() {
    }

    public CreateRoomRequest(String name, Integer gridRows, Integer gridCols, List<SeatDTO> seats) {
        this.name = name;
        this.gridRows = gridRows;
        this.gridCols = gridCols;
        this.seats = seats;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<SeatDTO> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatDTO> seats) {
        this.seats = seats;
    }
}
