package com.example.cinema.facility.infrastructure.database.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "seats", schema = "facility")
@SQLDelete(sql = "UPDATE facility.seats SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class SeatJpaEntity {
    @jakarta.persistence.Column(name = "is_deleted")
    private boolean isDeleted = false;

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
}
