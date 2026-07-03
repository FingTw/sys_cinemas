package com.example.cinema.catalog.infrastructure.database.repositories;

import com.example.cinema.catalog.infrastructure.database.entities.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataProductRepository extends JpaRepository<ProductJpaEntity, String> {
    List<ProductJpaEntity> findAllByActiveTrueAndIsDeletedFalseOrderByDisplayOrderAsc();
    List<ProductJpaEntity> findAllByIsDeletedFalseOrderByDisplayOrderAsc();
    List<ProductJpaEntity> findAllByCategoryIdAndActiveTrueAndIsDeletedFalseOrderByDisplayOrderAsc(String categoryId);
    Optional<ProductJpaEntity> findByIdAndIsDeletedFalse(String id);
}
