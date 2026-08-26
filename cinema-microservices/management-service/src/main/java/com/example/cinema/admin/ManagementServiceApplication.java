package com.example.cinema.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EntityScan({"com.example.cinema.admin", "com.example.cinema.common"})
@EnableJpaRepositories("com.example.cinema.admin")
@EnableFeignClients("com.example.cinema.admin")
@org.springframework.scheduling.annotation.EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.example.cinema.admin", "com.example.cinema.common"})
public class ManagementServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ManagementServiceApplication.class, args);
    }
}

