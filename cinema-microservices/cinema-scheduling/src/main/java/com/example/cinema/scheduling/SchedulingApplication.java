package com.example.cinema.scheduling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EntityScan({"com.example.cinema.scheduling", "com.example.cinema.common"})
@EnableJpaRepositories("com.example.cinema.scheduling")
@EnableFeignClients("com.example.cinema.scheduling")
@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.example.cinema.scheduling", "com.example.cinema.common"})
public class SchedulingApplication {
    public static void main(String[] args) {
        SpringApplication.run(SchedulingApplication.class, args);
    }
}
