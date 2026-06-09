package com.example.cinema.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/booking")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, Object>> bookingFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 503);
        response.put("error", "Service Unavailable");
        response.put("message", "Hệ thống đặt vé hiện đang quá tải hoặc tạm thời không phản hồi. Vui lòng thử lại sau ít phút.");
        return Mono.just(response);
    }
}
