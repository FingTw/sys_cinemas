package com.example.cinema.admin.infrastructure.database.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "cinemas", schema = "facility")
@SQLDelete(sql = "UPDATE facility.cinemas SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CinemaJpaEntity {

    @Builder.Default
    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(name = "complex_id", nullable = false)
    private String complexId;
}
