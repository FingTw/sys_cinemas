package com.example.cinema.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.cloud.openfeign.EnableFeignClients;


@EntityScan({"com.example.cinema.catalog", "com.example.cinema.common"})
@EnableJpaRepositories("com.example.cinema.catalog")
@EnableFeignClients("com.example.cinema.catalog")
@SpringBootApplication(scanBasePackages = {"com.example.cinema.catalog", "com.example.cinema.common"})
public class CatalogApplication {
    public static void main(String[] args) {
        SpringApplication.run(CatalogApplication.class, args);
    }
}
