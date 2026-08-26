package com.example.cinema.admin.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "showtimes", schema = "scheduling")
@SQLDelete(sql = "UPDATE scheduling.showtimes SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Showtime implements Persistable<String> {
    
    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return this.isNew || this.id == null;
    }

    @PrePersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    @Builder.Default
    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String movieId;

    @Column(nullable = false)
    private String roomId;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Builder.Default
    @Column(nullable = false)
    private String status = "SCHEDULED";

    @Builder.Default
    @Column(nullable = true)
    private BigDecimal price = new BigDecimal("75000");

    @Builder.Default
    @Column(nullable = true)
    private BigDecimal priceVip = new BigDecimal("120000");

    @Builder.Default
    @Column(nullable = true)
    private BigDecimal priceCouple = new BigDecimal("195000");

    public void updateDetails(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime, String movieId, java.math.BigDecimal basePrice, java.math.BigDecimal vipPrice, java.math.BigDecimal couplePrice) { this.startTime = startTime; this.endTime = endTime; this.movieId = movieId; this.price = basePrice; this.priceVip = vipPrice; this.priceCouple = couplePrice; }

}
