package com.example.cinema.admin.infrastructure.database.repositories;

import com.example.cinema.admin.infrastructure.database.entities.BannerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataBannerRepository extends JpaRepository<BannerJpaEntity, String> {
    List<BannerJpaEntity> findAllByOrderByDisplayOrderAsc();
}
