package com.example.cinema.catalog.infrastructure.database.repositories;

import com.example.cinema.catalog.infrastructure.database.entities.ServiceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataServiceRepository extends JpaRepository<ServiceJpaEntity, String> {
    List<ServiceJpaEntity> findAllByActiveTrueOrderByDisplayOrderAsc();
}
