package com.example.cinema.infrastructure.database.repositories;

import com.example.cinema.infrastructure.database.entities.BookingSeatJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataBookingSeatRepository extends JpaRepository<BookingSeatJpaEntity, String> {
}
