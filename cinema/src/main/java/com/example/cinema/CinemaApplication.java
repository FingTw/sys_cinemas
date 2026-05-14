package com.example.cinema;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class CinemaApplication {

	@Value("${APP_TIMEZONE}")
	private String timezone;

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone(timezone));
	}

	public static void main(String[] args) {
		SpringApplication.run(CinemaApplication.class, args);
	}
}
