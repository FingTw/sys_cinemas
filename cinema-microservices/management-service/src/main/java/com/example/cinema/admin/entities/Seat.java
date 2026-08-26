package com.example.cinema.admin.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "seats", schema = "facility")
@SQLDelete(sql = "UPDATE facility.seats SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {
    @Builder.Default
    @Column(name = "is_deleted")
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

    @Builder.Default
    @Column(nullable = false)
    private String type = "STANDARD";

    @Builder.Default
    @Column(nullable = false)
    private String status = "ACTIVE";

    public void updateDetails(String rowLabel, Integer colNumber, String type, String status) { this.rowLabel = rowLabel; this.colNumber = colNumber; this.type = type; this.status = status; }

}
