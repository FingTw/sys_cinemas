package com.example.cinema.admin.infrastructure.database.repositories;

import com.example.cinema.admin.infrastructure.database.entities.ServiceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataServiceRepository extends JpaRepository<ServiceJpaEntity, String> {
}
