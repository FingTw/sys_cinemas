package com.example.cinema.admin.infrastructure.database.repositories;

import com.example.cinema.admin.infrastructure.database.entities.ProductJpaEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataProductRepository extends JpaRepository<ProductJpaEntity, String> {
    List<ProductJpaEntity> findAllByIsDeletedFalse(Sort sort);
    Optional<ProductJpaEntity> findByIdAndIsDeletedFalse(String id);
}
