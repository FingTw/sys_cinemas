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
@SpringBootApplication(scanBasePackages = {"com.example.cinema.booking", "com.example.cinema.common"})
public class BookingApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookingApplication.class, args);
    }
}
