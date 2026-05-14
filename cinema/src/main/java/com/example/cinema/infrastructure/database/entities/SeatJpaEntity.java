package com.example.cinema.infrastructure.database.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "seats", schema = "cinema")
public class SeatJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String roomId;

    @Column(name = "seat_row", nullable = false)
    private String rowLabel;

    @Column(name = "seat_number", nullable = false)
    private Integer colNumber;

    @Column(nullable = false)
    private String type = "STANDARD";

    @Column(nullable = false)
    private String status = "ACTIVE";

    public SeatJpaEntity() {
    }

    public SeatJpaEntity(String id, String roomId, String rowLabel, Integer colNumber, String type, String status) {
        this.id = id;
        this.roomId = roomId;
        this.rowLabel = rowLabel;
        this.colNumber = colNumber;
        this.type = type;
        this.status = status;
    }

    // Builder manual
    public static SeatJpaEntityBuilder builder() {
        return new SeatJpaEntityBuilder();
    }

    public static class SeatJpaEntityBuilder {
        private String id;
        private String roomId;
        private String rowLabel;
        private Integer colNumber;
        private String type = "STANDARD";
        private String status = "ACTIVE";

        public SeatJpaEntityBuilder id(String id) {
            this.id = id;
            return this;
        }

        public SeatJpaEntityBuilder roomId(String roomId) {
            this.roomId = roomId;
            return this;
        }

        public SeatJpaEntityBuilder rowLabel(String rowLabel) {
            this.rowLabel = rowLabel;
            return this;
        }

        public SeatJpaEntityBuilder colNumber(Integer colNumber) {
            this.colNumber = colNumber;
            return this;
        }

        public SeatJpaEntityBuilder type(String type) {
            this.type = type;
            return this;
        }

        public SeatJpaEntityBuilder status(String status) {
            this.status = status;
            return this;
        }

        public SeatJpaEntity build() {
            return new SeatJpaEntity(id, roomId, rowLabel, colNumber, type, status);
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
