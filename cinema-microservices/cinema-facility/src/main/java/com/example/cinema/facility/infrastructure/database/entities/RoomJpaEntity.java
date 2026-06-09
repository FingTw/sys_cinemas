package com.example.cinema.facility.infrastructure.database.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rooms", schema = "facility")
@SQLDelete(sql = "UPDATE facility.rooms SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class RoomJpaEntity {
    @jakarta.persistence.Column(name = "is_deleted")
    private boolean isDeleted = false;

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
}
