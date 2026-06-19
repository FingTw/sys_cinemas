package com.example.cinema.catalog.infrastructure.database.repositories;

import com.example.cinema.catalog.infrastructure.database.entities.PromotionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataPromotionRepository extends JpaRepository<PromotionJpaEntity, String> {
    List<PromotionJpaEntity> findAllByActiveTrueOrderByDisplayOrderAsc();
}
