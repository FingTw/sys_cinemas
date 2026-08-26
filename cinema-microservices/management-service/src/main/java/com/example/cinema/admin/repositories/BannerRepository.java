package com.example.cinema.admin.repositories;

import com.example.cinema.admin.entities.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, String> {
    List<Banner> findAllByOrderByDisplayOrderAsc();
}
