package com.example.cinema.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.cloud.openfeign.EnableFeignClients;


import org.springframework.cloud.openfeign.EnableFeignClients;

@EntityScan({"com.example.cinema.booking", "com.example.cinema.common"})
@EnableJpaRepositories("com.example.cinema.booking")
@EnableFeignClients("com.example.cinema.booking")
@org.springframework.scheduling.annotation.EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.example.cinema.booking", "com.example.cinema.common"})
public class BookingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);
    }
}

