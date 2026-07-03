package com.example.cinema.admin.infrastructure.database.repositories;

import com.example.cinema.admin.infrastructure.database.entities.ProductCategoryJpaEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataProductCategoryRepository extends JpaRepository<ProductCategoryJpaEntity, String> {
    List<ProductCategoryJpaEntity> findAll(Sort sort);
}
