package com.example.cinema.infrastructure.database.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms", schema = "cinema")
public class RoomJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(columnDefinition = "integer default 10")
    private Integer gridRows = 10;

    @Column(columnDefinition = "integer default 15")
    private Integer gridCols = 15;

    public RoomJpaEntity() {
    }

    public RoomJpaEntity(String id, String name, String status, Integer gridRows, Integer gridCols) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.gridRows = gridRows;
        this.gridCols = gridCols;
    }

    // Builder manual (để không làm gãy các code đang gọi .builder())
    public static RoomJpaEntityBuilder builder() {
        return new RoomJpaEntityBuilder();
    }

    public static class RoomJpaEntityBuilder {
        private String id;
        private String name;
        private String status = "ACTIVE";
        private Integer gridRows = 10;
        private Integer gridCols = 15;

        public RoomJpaEntityBuilder id(String id) {
            this.id = id;
            return this;
        }

        public RoomJpaEntityBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RoomJpaEntityBuilder status(String status) {
            this.status = status;
            return this;
        }

        public RoomJpaEntityBuilder gridRows(Integer gridRows) {
            this.gridRows = gridRows;
            return this;
        }

        public RoomJpaEntityBuilder gridCols(Integer gridCols) {
            this.gridCols = gridCols;
            return this;
        }

        public RoomJpaEntity build() {
            return new RoomJpaEntity(id, name, status, gridRows, gridCols);
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
