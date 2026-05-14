package com.example.cinema.iam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.cloud.openfeign.EnableFeignClients;


@EntityScan({"com.example.cinema.iam", "com.example.cinema.common"})
@EnableJpaRepositories("com.example.cinema.iam")
@EnableFeignClients("com.example.cinema.iam")
@SpringBootApplication(scanBasePackages = {"com.example.cinema.iam", "com.example.cinema.common"})
public class IamApplication {
    public static void main(String[] args) {
        SpringApplication.run(IamApplication.class, args);
    }
}
