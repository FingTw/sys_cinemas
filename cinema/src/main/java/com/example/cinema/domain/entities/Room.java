package com.example.cinema.domain.entities;

public class Room {
    private String id;
    private String name;
    private String status; // ACTIVE, MAINTENANCE
    private Integer gridRows;
    private Integer gridCols;

    public Room() {
    }

    public Room(String id, String name, String status, Integer gridRows, Integer gridCols) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.gridRows = gridRows;
        this.gridCols = gridCols;
    }

    // Builder manual
    public static RoomBuilder builder() {
        return new RoomBuilder();
    }

    public static class RoomBuilder {
        private String id;
        private String name;
        private String status;
        private Integer gridRows;
        private Integer gridCols;

        public RoomBuilder id(String id) {
            this.id = id;
            return this;
        }

        public RoomBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RoomBuilder status(String status) {
            this.status = status;
            return this;
        }

        public RoomBuilder gridRows(Integer gridRows) {
            this.gridRows = gridRows;
            return this;
        }

        public RoomBuilder gridCols(Integer gridCols) {
            this.gridCols = gridCols;
            return this;
        }

        public Room build() {
            return new Room(id, name, status, gridRows, gridCols);
        }
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
}
