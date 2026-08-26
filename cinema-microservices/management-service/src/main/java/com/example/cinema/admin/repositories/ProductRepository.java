package com.example.cinema.admin.repositories;

import com.example.cinema.admin.entities.Product;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findAllByIsDeletedFalse(Sort sort);
    Optional<Product> findByIdAndIsDeletedFalse(String id);
}
