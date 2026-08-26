package com.example.cinema.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan({ "com.example.cinema.iam", "com.example.cinema.common", "com.example.cinema.auth" })
@EnableJpaRepositories({ "com.example.cinema.iam", "com.example.cinema.auth" })
@EnableFeignClients("com.example.cinema.iam")
@SpringBootApplication(scanBasePackages = {
        "com.example.cinema.iam",
        "com.example.cinema.common",
        "com.example.cinema.auth"
})
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}