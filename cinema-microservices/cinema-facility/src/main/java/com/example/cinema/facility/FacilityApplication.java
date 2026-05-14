package com.example.cinema.facility;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.cloud.openfeign.EnableFeignClients;


@EntityScan({"com.example.cinema.facility", "com.example.cinema.common"})
@EnableJpaRepositories("com.example.cinema.facility")
@EnableFeignClients("com.example.cinema.facility")
@SpringBootApplication(scanBasePackages = {"com.example.cinema.facility", "com.example.cinema.common"})
public class FacilityApplication {
    public static void main(String[] args) {
        SpringApplication.run(FacilityApplication.class, args);
    }
}
